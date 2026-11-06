package com.nimmda.application.business;

import java.math.BigDecimal;
import java.util.List;

public record BusinessListingView(
        String id,
        String title,
        BigDecimal price,
        String imageSrc,
        List<String> images,
        String category
) {
}
