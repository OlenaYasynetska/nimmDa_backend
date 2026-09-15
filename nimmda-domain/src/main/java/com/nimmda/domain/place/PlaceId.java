package com.nimmda.domain.place;

import java.util.Objects;
import java.util.UUID;

public record PlaceId(String value) {

    public PlaceId {
        Objects.requireNonNull(value, "placeId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("placeId must not be blank");
        }
        value = value.trim();
    }

    public static PlaceId newId() {
        return new PlaceId(UUID.randomUUID().toString());
    }
}
