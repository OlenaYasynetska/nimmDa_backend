package com.nimmda.application.port.security;

public record AccessTokenPrincipal(String userId, String role) {
}
