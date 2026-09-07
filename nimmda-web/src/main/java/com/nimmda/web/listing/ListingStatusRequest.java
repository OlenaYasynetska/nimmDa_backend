package com.nimmda.web.listing;

import jakarta.validation.constraints.NotBlank;

public record ListingStatusRequest(@NotBlank String status) {
}
