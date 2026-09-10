package com.nimmda.domain.user;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public final class User {

    private final UserId id;
    private final String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private final UserRole role;
    private final AccountMode accountMode;
    private boolean emailVerified;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            UserRole role,
            AccountMode accountMode,
            boolean emailVerified,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = normalizeEmail(email);
        this.passwordHash = requireHash(passwordHash);
        this.firstName = requireName(firstName, "firstName");
        this.lastName = lastName == null ? "" : lastName.trim();
        this.role = Objects.requireNonNull(role);
        this.accountMode = Objects.requireNonNull(accountMode);
        this.emailVerified = emailVerified;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static User register(
            String email,
            String passwordHash,
            String firstName,
            String lastName
    ) {
        Instant now = Instant.now();
        return new User(
                UserId.newId(),
                email,
                passwordHash,
                firstName,
                lastName,
                UserRole.USER,
                AccountMode.BOTH,
                false,
                now,
                now
        );
    }

    public static User codedAdmin(String email) {
        Instant now = Instant.now();
        return new User(
                new UserId(CodedAdmin.ID),
                email,
                "coded-admin",
                CodedAdmin.FIRST_NAME,
                CodedAdmin.LAST_NAME,
                UserRole.ADMIN,
                AccountMode.BOTH,
                true,
                now,
                now
        );
    }

    public static User rehydrate(
            UserId id,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            UserRole role,
            AccountMode accountMode,
            boolean emailVerified,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(
                id,
                email,
                passwordHash,
                firstName,
                lastName,
                role,
                accountMode,
                emailVerified,
                createdAt,
                updatedAt
        );
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }

    public void replacePassword(String passwordHash) {
        this.passwordHash = requireHash(passwordHash);
        this.updatedAt = Instant.now();
    }

    public UserId id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public UserRole role() {
        return role;
    }

    public AccountMode accountMode() {
        return accountMode;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String requireHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        return passwordHash;
    }

    private static String requireName(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
