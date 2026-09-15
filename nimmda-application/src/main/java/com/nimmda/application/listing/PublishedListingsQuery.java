package com.nimmda.application.listing;

import com.nimmda.domain.geo.GeoCoordinates;
import com.nimmda.domain.listing.ListingSearch;
import com.nimmda.domain.listing.ListingSort;

import java.math.BigDecimal;

public record PublishedListingsQuery(
        String q,
        String category,
        String location,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sort,
        boolean free,
        Integer km,
        GeoCoordinates origin,
        int page,
        int size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PublishedListingsQuery {
        page = Math.max(page, 0);
        size = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        km = normalizeKm(km);
    }

    public PublishedListingsQuery(
            String q,
            String category,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            boolean free,
            Integer km,
            int page,
            int size
    ) {
        this(q, category, location, minPrice, maxPrice, sort, free, km, null, page, size);
    }

    public PublishedListingsQuery withOrigin(GeoCoordinates origin) {
        return new PublishedListingsQuery(q, category, location, minPrice, maxPrice, sort, free, km, origin, page, size);
    }

    public ListingSearch toSearch() {
        BigDecimal min = minPrice;
        BigDecimal max = maxPrice;
        if (free) {
            min = BigDecimal.ZERO;
            max = BigDecimal.ZERO;
        }
        String cityFilter = location;
        Integer radius = km;
        ListingSort listingSort = ListingSort.from(sort);
        if (origin != null && (radius != null || listingSort == ListingSort.DISTANCE)) {
            cityFilter = radius != null ? null : location;
            if (listingSort == ListingSort.DISTANCE && radius == null) {
                cityFilter = null;
            }
        }
        return new ListingSearch(q, category, cityFilter, min, max, listingSort, origin, radius);
    }

    private static Integer normalizeKm(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 1 || value > 200) {
            return null;
        }
        return value;
    }
}
