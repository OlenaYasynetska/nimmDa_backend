package com.nimmda.domain.listing;

public enum ListingSort {
    NEWEST,
    OLDEST,
    PRICE_ASC,
    PRICE_DESC,
    DISTANCE;

    public static ListingSort from(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }
        return switch (value.trim().toLowerCase()) {
            case "oldest" -> OLDEST;
            case "price_asc" -> PRICE_ASC;
            case "price_desc" -> PRICE_DESC;
            case "distance", "naehe", "nähe" -> DISTANCE;
            default -> NEWEST;
        };
    }
}
