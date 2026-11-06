package com.nimmda.application.listing;

import java.math.BigDecimal;
import java.util.List;

public record UpdateListingCommand(
        String listingId,
        String actorId,
        String title,
        BigDecimal price,
        String category,
        String location,
        String imageSrc,
        List<String> images,
        String description,
        String status
) {
}
