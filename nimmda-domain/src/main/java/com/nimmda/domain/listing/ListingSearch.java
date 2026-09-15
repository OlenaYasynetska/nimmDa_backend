package com.nimmda.domain.listing;

import com.nimmda.domain.geo.GeoCoordinates;

import java.math.BigDecimal;
import java.util.Locale;

public record ListingSearch(
        String text,
        String category,
        String location,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ListingSort sort,
        GeoCoordinates origin,
        Integer radiusKm
) {
    public ListingSearch {
        text = blankToNull(text);
        category = blankToNull(category);
        location = blankToNull(location);
        minPrice = nonNegative(minPrice);
        maxPrice = nonNegative(maxPrice);
        sort = sort == null ? ListingSort.NEWEST : sort;
        radiusKm = normalizeRadius(radiusKm);
        if (origin == null) {
            radiusKm = null;
            if (sort == ListingSort.DISTANCE) {
                sort = ListingSort.NEWEST;
            }
        }
        if (radiusKm != null) {
            location = null;
        }
    }

    public static ListingSearch allPublished() {
        return new ListingSearch(null, null, null, null, null, ListingSort.NEWEST, null, null);
    }

    public boolean hasRadius() {
        return origin != null && radiusKm != null;
    }

    private static Integer normalizeRadius(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 1 || value > 200) {
            return null;
        }
        return value;
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return null;
        }
        return value;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 80) {
            trimmed = trimmed.substring(0, 80);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String textLike() {
        if (text == null) {
            return null;
        }
        return "%" + text.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                + "%";
    }
}
