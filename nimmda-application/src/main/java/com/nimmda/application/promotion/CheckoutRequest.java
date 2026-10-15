package com.nimmda.application.promotion;

public record CheckoutRequest(
        String orderId,
        String kind,
        String productName,
        String description,
        String sellerEmail,
        int amountCents,
        String currency,
        String successUrl,
        String cancelUrl
) {
}
