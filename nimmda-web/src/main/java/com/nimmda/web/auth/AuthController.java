package com.nimmda.web.auth;

import com.nimmda.application.auth.AuthSession;
import com.nimmda.application.auth.ConfirmEmailUseCase;
import com.nimmda.application.auth.LoginUserCommand;
import com.nimmda.application.auth.LoginUserUseCase;
import com.nimmda.application.auth.RegisterUserCommand;
import com.nimmda.application.auth.RegisterUserResult;
import com.nimmda.application.auth.RegisterUserUseCase;
import com.nimmda.application.auth.RequestPasswordResetUseCase;
import com.nimmda.application.auth.ResendVerificationUseCase;
import com.nimmda.application.auth.ResetPasswordCommand;
import com.nimmda.application.auth.ResetPasswordUseCase;
import com.nimmda.application.auth.VerifyEmailUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            ConfirmEmailUseCase confirmEmailUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            ResendVerificationUseCase resendVerificationUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.confirmEmailUseCase = confirmEmailUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserResult result = registerUserUseCase.register(
                new RegisterUserCommand(request.email(), request.password())
        );
        return new RegisterResponse(result.mailSent(), result.verifyUrl());
    }

    @PostMapping("/login")
    public AuthSessionResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(loginUserUseCase.login(
                new LoginUserCommand(request.email(), request.password())
        ));
    }

    @GetMapping("/verify-email")
    public VerifyEmailResponse confirmEmail(@RequestParam("token") String token) {
        confirmEmailUseCase.confirm(token);
        return new VerifyEmailResponse("E-Mail bestätigt. Du kannst dich jetzt anmelden.", true);
    }

    @PostMapping("/verify")
    public AuthSessionResponse verify(@Valid @RequestBody TokenRequest request) {
        return toResponse(verifyEmailUseCase.verify(request.token()));
    }

    @PostMapping("/forgot-password")
    public RegisterResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        RegisterUserResult result = requestPasswordResetUseCase.requestReset(request.email());
        return new RegisterResponse(result.mailSent(), result.verifyUrl());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.reset(new ResetPasswordCommand(request.token(), request.password()));
    }

    @PostMapping("/resend-verification")
    public RegisterResponse resend(@Valid @RequestBody EmailRequest request) {
        RegisterUserResult result = resendVerificationUseCase.resend(request.email());
        return new RegisterResponse(result.mailSent(), result.verifyUrl());
    }

    private static AuthSessionResponse toResponse(AuthSession session) {
        return new AuthSessionResponse(
                session.id(),
                session.email(),
                session.firstName(),
                session.lastName(),
                session.role(),
                session.accountMode(),
                session.accessToken(),
                session.expiresAt()
        );
    }
}
