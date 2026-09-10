package com.nimmda.web.security;

import com.nimmda.application.auth.AuthException;
import org.springframework.security.core.Authentication;

public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static String id(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new AuthException("invalid");
        }
        String userId = authentication.getName();
        if (userId == null || userId.isBlank() || "anonymousUser".equals(userId)) {
            throw new AuthException("invalid");
        }
        return userId;
    }
}
