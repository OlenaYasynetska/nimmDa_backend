package com.nimmda.web.listing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateListingRequest(
        @NotBlank String title,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank String category,
        @NotBlank String location,
        String imageSrc,
        String status
) {
}
