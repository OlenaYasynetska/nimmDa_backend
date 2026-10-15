package com.nimmda.application.promotion;

public record PaidCheckout(String orderId, String providerSessionId, boolean paid, String kind) {
}
