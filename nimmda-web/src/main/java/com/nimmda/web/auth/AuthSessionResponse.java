package com.nimmda.web.auth;

public record AuthSessionResponse(
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
