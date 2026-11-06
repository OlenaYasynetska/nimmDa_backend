package com.nimmda.web.listing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateListingRequest(
        @NotBlank String title,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank String category,
        @NotBlank String location,
        String imageSrc,
        @Size(max = 5) List<String> images,
        @Size(max = 4000) String description,
        String status
) {
}
