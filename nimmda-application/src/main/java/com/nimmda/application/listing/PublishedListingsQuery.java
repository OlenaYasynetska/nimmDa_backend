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
        boolean free
) {
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
