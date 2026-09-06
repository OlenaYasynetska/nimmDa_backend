package com.nimmda.application.auth;

public record AuthSession(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        String accountMode,
        String accessToken,
        long expiresAt
) {
}
