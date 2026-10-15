package com.nimmda.application.business;

import java.time.Instant;

public record BusinessSubscriptionView(
        String id,
        String userId,
        String companyName,
        String sellerEmail,
        int amountCents,
        String currency,
        Instant paidAt,
        Instant activeUntil
) {
}
