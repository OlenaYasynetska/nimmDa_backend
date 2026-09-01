package com.nimmda.domain.listing;

import java.util.Objects;

public record Location(String city) {

    public Location {
        Objects.requireNonNull(city, "location must not be null");
        String normalized = city.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        city = normalized;
    }
}
