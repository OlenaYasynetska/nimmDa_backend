package com.nimmda.domain.shared;

import java.util.Objects;
import java.util.UUID;

public record UserId(String value) {

    public UserId {
        Objects.requireNonNull(value, "userId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        value = value.trim();
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }
}
