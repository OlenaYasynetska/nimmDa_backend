package com.nimmda.application.port.geo;

import com.nimmda.domain.geo.GeoCoordinates;

public record GeocodedPlace(
        String name,
        String region,
        String country,
        String postalCode,
        GeoCoordinates coordinates,
        String displayName
) {
}
