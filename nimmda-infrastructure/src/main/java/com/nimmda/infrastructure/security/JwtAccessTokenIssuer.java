package com.nimmda.infrastructure.security;

import com.nimmda.application.port.security.AccessTokenIssuer;
import com.nimmda.application.port.security.AccessTokenPrincipal;
import com.nimmda.application.port.security.IssuedAccessToken;
import com.nimmda.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final SecretKey key;
    private final long ttlMs;

    public JwtAccessTokenIssuer(
            @Value("${app.jwt.secret:nimmda-dev-jwt-secret-change-me-please-32}") String secret,
            @Value("${app.jwt.ttl-ms:86400000}") long ttlMs
    ) {
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.ttlMs = ttlMs;
    }

    @Override
    public IssuedAccessToken issue(User user) {
        long expiresAt = Instant.now().toEpochMilli() + ttlMs;
        String token = Jwts.builder()
                .subject(user.id().value())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .expiration(new Date(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedAccessToken(token, expiresAt);
    }

    @Override
    public Optional<AccessTokenPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new AccessTokenPrincipal(
                    claims.getSubject(),
                    claims.get("role", String.class)
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot initialize JWT key", ex);
        }
    }
}
