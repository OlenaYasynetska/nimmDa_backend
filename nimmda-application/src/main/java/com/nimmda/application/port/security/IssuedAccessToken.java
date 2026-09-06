package com.nimmda.application.port.security;

public record IssuedAccessToken(String token, long expiresAtEpochMs) {
}
