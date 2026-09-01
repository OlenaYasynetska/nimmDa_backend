package com.nimmda.domain.listing;

import java.util.Objects;

public record Category(String name) {

    public Category {
        Objects.requireNonNull(name, "category must not be null");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("category must not be blank");
        }
        name = normalized;
    }
}
