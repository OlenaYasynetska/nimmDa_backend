package com.nimmda.domain.listing;

import java.math.BigDecimal;
import java.util.Locale;

public record ListingSearch(
        String text,
        String category,
        String location,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ListingSort sort
) {
    public ListingSearch {
        text = blankToNull(text);
        category = blankToNull(category);
        location = blankToNull(location);
        minPrice = nonNegative(minPrice);
        maxPrice = nonNegative(maxPrice);
        sort = sort == null ? ListingSort.NEWEST : sort;
    }

    public static ListingSearch allPublished() {
        return new ListingSearch(null, null, null, null, null, ListingSort.NEWEST);
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
