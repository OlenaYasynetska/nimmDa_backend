package com.nimmda.domain.place;

import com.nimmda.domain.geo.GeoCoordinates;

import java.util.Objects;

public record Place(
        PlaceId id,
        String name,
        String region,
        String country,
        String postalCode,
        GeoCoordinates coordinates
) {
    public Place {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(coordinates, "coordinates must not be null");
        name = requireText(name, "name");
        region = region == null ? "" : region.trim();
        country = requireText(country, "country").toUpperCase();
        if (country.length() > 2) {
            country = country.substring(0, 2);
        }
        postalCode = postalCode == null || postalCode.isBlank() ? null : postalCode.trim();
    }

    public String nameNormalized() {
        return PlaceNames.normalize(name);
    }

    public String displayName() {
        if (region.isBlank()) {
            return name;
        }
        return name + ", " + region;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
