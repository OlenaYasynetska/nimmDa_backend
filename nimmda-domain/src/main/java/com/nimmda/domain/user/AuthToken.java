package com.nimmda.domain.user;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AuthToken {

    private final String id;
    private final UserId userId;
    private final String token;
    private final AuthTokenType type;
    private final Instant expiresAt;
    private Instant consumedAt;
    private final Instant createdAt;

    private AuthToken(
            String id,
            UserId userId,
            String token,
            AuthTokenType type,
            Instant expiresAt,
            Instant consumedAt,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.token = Objects.requireNonNull(token);
        this.type = Objects.requireNonNull(type);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.consumedAt = consumedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static AuthToken issue(UserId userId, AuthTokenType type, Instant expiresAt) {
        Instant now = Instant.now();
        return new AuthToken(
                UUID.randomUUID().toString(),
                userId,
                UUID.randomUUID().toString().replace("-", ""),
                type,
                expiresAt,
                null,
                now
        );
    }

    public static AuthToken rehydrate(
            String id,
            UserId userId,
            String token,
            AuthTokenType type,
            Instant expiresAt,
            Instant consumedAt,
            Instant createdAt
    ) {
        return new AuthToken(id, userId, token, type, expiresAt, consumedAt, createdAt);
    }

    public void consume() {
        this.consumedAt = Instant.now();
    }

    public boolean usable(Instant now) {
        return consumedAt == null && now.isBefore(expiresAt);
    }

    public String id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String token() {
        return token;
    }

    public AuthTokenType type() {
        return type;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant consumedAt() {
        return consumedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
