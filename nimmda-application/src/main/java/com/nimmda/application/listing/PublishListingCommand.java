package com.nimmda.application.listing;

import java.math.BigDecimal;

public record PublishListingCommand(
        String sellerId,
        String title,
        BigDecimal price,
        String category,
        String location,
        String imageSrc
) {
}
