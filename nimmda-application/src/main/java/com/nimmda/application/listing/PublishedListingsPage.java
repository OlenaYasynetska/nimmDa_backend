package com.nimmda.application.listing;

import java.util.List;

public record PublishedListingsPage(
        List<ListingView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
