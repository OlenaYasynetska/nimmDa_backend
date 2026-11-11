package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ListingPhotoAddresses {

    private static ListingPhotoAddresses instance;

    private final String origin;

    public ListingPhotoAddresses(@Value("${app.api.public-base-url:https://api.nimmda.org}") String origin) {
        this.origin = stripTrailingSlash(origin);
        instance = this;
    }

    public static List<String> forResponse(Listing listing) {
        List<String> images = listing.images();
        ListingPhotoAddresses current = instance;
        if (current == null || images.stream().noneMatch(src -> src.startsWith("data:"))) {
            return images;
        }
        return current.links(listing.id().value(), images.size(), listing.updatedAt());
    }

    public List<String> links(String listingId, int count, Instant updatedAt) {
        int safe = Math.max(1, Math.min(count, 5));
        long version = updatedAt == null ? 0L : updatedAt.toEpochMilli();
        List<String> urls = new ArrayList<>(safe);
        for (int index = 0; index < safe; index++) {
            urls.add(origin + "/api/listings/" + listingId + "/photos/" + index + "?v=" + version);
        }
        return List.copyOf(urls);
    }

    public Integer storedIndex(String listingId, String value) {
        if (listingId == null || listingId.isBlank() || value == null || value.isBlank()) {
            return null;
        }
        String marker = "/listings/" + listingId.trim() + "/photos/";
        int at = value.indexOf(marker);
        if (at < 0) {
            return null;
        }
        int start = at + marker.length();
        int end = start;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == start) {
            return null;
        }
        try {
            return Integer.parseInt(value.substring(start, end));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String stripTrailingSlash(String value) {
        String trimmed = value == null ? "" : value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isBlank() ? "https://api.nimmda.org" : trimmed;
    }
}
