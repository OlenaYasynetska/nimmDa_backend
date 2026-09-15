package com.nimmda.application.place;

public record PlaceView(
        String name,
        String region,
        String postalCode,
        String displayName
) {
}
