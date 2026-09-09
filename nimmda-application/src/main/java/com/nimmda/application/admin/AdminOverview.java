package com.nimmda.application.admin;

public record AdminOverview(
        long buyerCount,
        long sellerCount,
        long listingCount,
        long paymentCount,
        long advertiserCount,
        long subscriptionCount
) {
}
