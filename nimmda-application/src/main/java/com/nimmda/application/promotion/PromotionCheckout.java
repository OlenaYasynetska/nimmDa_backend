package com.nimmda.application.promotion;

public interface PromotionCheckout {

    boolean configured();

    StartedCheckout start(CheckoutRequest request);

    PaidCheckout retrieve(String providerSessionId);
}
