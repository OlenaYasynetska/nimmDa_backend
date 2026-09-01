package com.nimmda.web.messaging;

import jakarta.validation.constraints.NotBlank;

public record SendInquiryRequest(
        @NotBlank String buyerId,
        @NotBlank String buyerName,
        @NotBlank String message
) {
}
