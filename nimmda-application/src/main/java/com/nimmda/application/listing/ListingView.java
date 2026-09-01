package com.nimmda.application.listing;

import java.math.BigDecimal;
import java.time.Instant;

public record ListingView(
        String id,
        String sellerId,
        String title,
        BigDecimal price,
        String category,
        String location,
        String imageSrc,
        String status,
        int views,
        int chats,
        Instant createdAt
) {
}
