package com.nimmda.application.listing;

import java.math.BigDecimal;
import java.util.List;

public record PublishListingCommand(
        String sellerId,
        String title,
        BigDecimal price,
        String category,
        String location,
        String imageSrc,
        List<String> images,
        String description
) {
}
