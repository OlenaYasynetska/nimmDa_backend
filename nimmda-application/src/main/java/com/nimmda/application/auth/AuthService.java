package com.nimmda.application.auth;

import com.nimmda.application.mail.SendAuthMailCommand;
import com.nimmda.application.mail.SendAuthMailUseCase;
import com.nimmda.application.port.auth.FrontendAuthLinks;
import com.nimmda.application.port.security.AccessTokenIssuer;
import com.nimmda.application.port.security.IssuedAccessToken;
import com.nimmda.application.port.security.PasswordHasher;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.AccountMode;
import com.nimmda.domain.user.AuthToken;
import com.nimmda.domain.user.AuthTokenRepository;
import com.nimmda.domain.user.AuthTokenType;
import com.nimmda.domain.user.CodedAdmin;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import com.nimmda.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService implements
        RegisterUserUseCase,
        LoginUserUseCase,
        VerifyEmailUseCase,
        RequestPasswordResetUseCase,
        ResetPasswordUseCase,
        ResendVerificationUseCase,
        UpdateAccountModeUseCase,
        ConfirmEmailUseCase {

    private static final Duration VERIFY_TTL = Duration.ofHours(24);
    private static final Duration RESET_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final SendAuthMailUseCase sendAuthMailUseCase;
    private final FrontendAuthLinks frontendAuthLinks;
    private final boolean exposeDevLinks;

    public AuthService(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            SendAuthMailUseCase sendAuthMailUseCase,
            FrontendAuthLinks frontendAuthLinks,
            @Value("${app.auth.expose-dev-links:false}") boolean exposeDevLinks
    ) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.sendAuthMailUseCase = sendAuthMailUseCase;
        this.frontendAuthLinks = frontendAuthLinks;
        this.exposeDevLinks = exposeDevLinks;
    }

    @Override
    @Transactional
    public RegisterUserResult register(RegisterUserCommand command) {
        requirePassword(command.password());
        String email = User.normalizeEmail(command.email());
        if (CodedAdmin.isEmail(email)) {
            throw new AuthException("exists");
        }
        AccountMode mode = parseMode(command.accountMode());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.emailVerified()) {
            throw new AuthException("exists");
        }
        String hash = passwordHasher.hash(command.password());
        if (user == null) {
            NameParts names = namesFromEmail(email);
            user = User.register(email, hash, names.firstName(), names.lastName(), mode);
        } else {
            user.replacePassword(hash);
            user.changeAccountMode(mode);
        }
        userRepository.save(user);
        return issueMail(user, AuthTokenType.VERIFY);
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public AuthSession login(LoginUserCommand command) {
        String email = User.normalizeEmail(command.email());
        if (CodedAdmin.isEmail(email)) {
            if (!CodedAdmin.matches(email, command.password())) {
                throw new AuthException("invalid");
            }
            return toSession(User.codedAdmin(parseMode(command.accountMode())));
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AuthException("notFound"));
        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new AuthException("invalid");
        }
        if (!user.emailVerified()) {
            try {
                issueMail(user, AuthTokenType.VERIFY);
            } catch (AuthException ex) {
                if (!"mailFailed".equals(ex.code())) {
                    throw ex;
                }
            }
            throw new AuthException("unverified");
        }
        if (command.accountMode() != null && !command.accountMode().isBlank()) {
            user.changeAccountMode(parseMode(command.accountMode()));
        }
        userRepository.save(user);
        return toSession(user);
    }

    @Override
    @Transactional
    public AuthSession verify(String token) {
        return toSession(markEmailVerified(token));
    }

    @Override
    @Transactional
    public void confirm(String token) {
        markEmailVerified(token);
    }

    private User markEmailVerified(String token) {
        AuthToken authToken = authTokenRepository
                .findUsableByToken(token, AuthTokenType.VERIFY)
                .orElseThrow(() -> new AuthException("expired"));
        if (!authToken.usable(Instant.now())) {
            throw new AuthException("expired");
        }
        User user = userRepository
                .findById(authToken.userId())
                .orElseThrow(() -> new AuthException("expired"));
        user.verifyEmail();
        userRepository.save(user);
        authToken.consume();
        authTokenRepository.save(authToken);
        return user;
    }

    @Override
    @Transactional
    public RegisterUserResult requestReset(String email) {
        String normalized = User.normalizeEmail(email);
        if (CodedAdmin.isEmail(normalized)) {
            return new RegisterUserResult(true, null);
        }
        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null) {
            return new RegisterUserResult(true, null);
        }
        return issueMail(user, AuthTokenType.RESET);
    }

    @Override
    @Transactional
    public void reset(ResetPasswordCommand command) {
        requirePassword(command.password());
        AuthToken authToken = authTokenRepository
                .findUsableByToken(command.token(), AuthTokenType.RESET)
                .orElseThrow(() -> new AuthException("expired"));
        if (!authToken.usable(Instant.now())) {
            throw new AuthException("expired");
        }
        User user = userRepository
                .findById(authToken.userId())
                .orElseThrow(() -> new AuthException("expired"));
        user.replacePassword(passwordHasher.hash(command.password()));
        userRepository.save(user);
        authToken.consume();
        authTokenRepository.save(authToken);
    }

    @Override
    @Transactional
    public RegisterUserResult resend(String email) {
        String normalized = User.normalizeEmail(email);
        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null || user.emailVerified() || CodedAdmin.isEmail(normalized)) {
            return new RegisterUserResult(true, null);
        }
        return issueMail(user, AuthTokenType.VERIFY);
    }

    @Override
    @Transactional
    public AuthSession updateMode(String userId, String accountMode) {
        AccountMode mode = parseMode(accountMode);
        if (CodedAdmin.isId(userId)) {
            return toSession(User.codedAdmin(mode));
        }
        User user = userRepository
                .findById(new UserId(userId))
                .orElseThrow(() -> new AuthException("notFound"));
        user.changeAccountMode(mode);
        userRepository.save(user);
        return toSession(user);
    }

    private RegisterUserResult issueMail(User user, AuthTokenType type) {
        authTokenRepository.deleteOpenTokens(user.id().value(), type);
        Duration ttl = type == AuthTokenType.RESET ? RESET_TTL : VERIFY_TTL;
        AuthToken token = AuthToken.issue(user.id(), type, Instant.now().plus(ttl));
        authTokenRepository.save(token);
        String link = type == AuthTokenType.RESET
                ? frontendAuthLinks.resetUrl(token.token())
                : frontendAuthLinks.verifyUrl(token.token());
        String mailType = type == AuthTokenType.RESET ? "reset" : "verify";
        boolean sent = sendAuthMailUseCase.execute(new SendAuthMailCommand(user.email(), mailType, link));
        if (!sent && !exposeDevLinks) {
            throw new AuthException("mailFailed");
        }
        return new RegisterUserResult(sent, exposeDevLinks ? link : null);
    }

    private AuthSession toSession(User user) {
        IssuedAccessToken issued = accessTokenIssuer.issue(user);
        return new AuthSession(
                user.id().value(),
                user.email(),
                user.firstName(),
                user.lastName(),
                frontendRole(user),
                user.accountMode().name().toLowerCase(Locale.ROOT),
                issued.token(),
                issued.expiresAtEpochMs()
        );
    }

    private static String frontendRole(User user) {
        if (user.role() == UserRole.ADMIN) {
            return "both";
        }
        return "both";
    }

    private static AccountMode parseMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return AccountMode.BOTH;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "buyer" -> AccountMode.BUYER;
            case "seller" -> AccountMode.SELLER;
            case "both" -> AccountMode.BOTH;
            default -> AccountMode.BOTH;
        };
    }

    private static void requirePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
    }

    private static NameParts namesFromEmail(String email) {
        String local = email.split("@")[0];
        if (local == null || local.isBlank()) {
            return new NameParts("Mitglied", "");
        }
        String[] parts = local.split("[._-]+");
        String first = capitalize(parts[0]);
        String last = parts.length > 1
                ? capitalize(String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)))
                : "";
        return new NameParts(first.isBlank() ? "Mitglied" : first, last);
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private record NameParts(String firstName, String lastName) {
    }
}
