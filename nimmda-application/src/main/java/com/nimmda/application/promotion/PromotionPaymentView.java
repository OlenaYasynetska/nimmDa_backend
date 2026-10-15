package com.nimmda.application.promotion;

import java.time.Instant;

public record PromotionPaymentView(
        String id,
        String listingId,
        String listingTitle,
        String sellerEmail,
        int amountCents,
        String currency,
        Instant paidAt,
        Instant promotedUntil
) {
}
