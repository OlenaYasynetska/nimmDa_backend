package com.nimmda.application.listing;

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
        int page,
        int size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PublishedListingsQuery {
        page = Math.max(page, 0);
        size = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    public ListingSearch toSearch() {
        BigDecimal min = minPrice;
        BigDecimal max = maxPrice;
        if (free) {
            min = BigDecimal.ZERO;
            max = BigDecimal.ZERO;
        }
        return new ListingSearch(q, category, location, min, max, ListingSort.from(sort));
    }
}
