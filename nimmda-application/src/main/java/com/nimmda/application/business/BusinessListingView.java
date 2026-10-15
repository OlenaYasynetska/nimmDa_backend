package com.nimmda.application.business;

import java.math.BigDecimal;

public record BusinessListingView(
        String id,
        String title,
        BigDecimal price,
        String imageSrc,
        String category
) {
}
