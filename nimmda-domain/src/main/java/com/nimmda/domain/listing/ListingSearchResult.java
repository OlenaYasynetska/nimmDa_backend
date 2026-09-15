package com.nimmda.domain.listing;

import java.util.List;

public record ListingSearchResult(
        List<Listing> content,
        int page,
        int size,
        long totalElements
) {
    public ListingSearchResult {
        content = content == null ? List.of() : List.copyOf(content);
        page = Math.max(page, 0);
        size = size < 1 ? 20 : size;
        totalElements = Math.max(totalElements, 0);
    }

    public int totalPages() {
        if (size <= 0 || totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}
