package com.nimmda.domain.listing;

import com.nimmda.domain.geo.GeoCoordinates;

import java.util.Objects;

public record Location(String city, GeoCoordinates coordinates) {

    public Location {
        Objects.requireNonNull(city, "location must not be null");
        String normalized = city.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        city = normalized;
    }

    public Location(String city) {
        this(city, null);
    }

    public Location withCoordinates(GeoCoordinates coordinates) {
        return new Location(city, coordinates);
    }
}
