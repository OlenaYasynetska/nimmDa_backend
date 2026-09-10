package com.nimmda.application.listing;

import java.math.BigDecimal;

public record UpdateListingCommand(
        String listingId,
        String actorId,
        String title,
        BigDecimal price,
        String category,
        String location,
        String imageSrc,
        String status
) {
}
