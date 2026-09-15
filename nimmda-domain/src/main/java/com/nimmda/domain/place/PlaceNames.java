package com.nimmda.domain.place;

import java.util.Locale;

public final class PlaceNames {

    private PlaceNames() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.GERMAN);
    }

    public static boolean looksLikePostalCode(String value) {
        return value != null && value.trim().matches("\\d{4}");
    }
}
