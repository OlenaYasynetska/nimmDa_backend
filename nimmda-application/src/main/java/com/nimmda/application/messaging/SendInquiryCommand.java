package com.nimmda.application.messaging;

public record SendInquiryCommand(
        String listingId,
        String buyerId,
        String message
) {
}
