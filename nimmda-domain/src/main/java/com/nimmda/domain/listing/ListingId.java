package com.nimmda.domain.listing;

import java.util.Objects;
import java.util.UUID;

public record ListingId(String value) {

    public ListingId {
        Objects.requireNonNull(value, "listingId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("listingId must not be blank");
        }
        value = value.trim();
    }

    public static ListingId newId() {
        return new ListingId(UUID.randomUUID().toString());
    }
}
