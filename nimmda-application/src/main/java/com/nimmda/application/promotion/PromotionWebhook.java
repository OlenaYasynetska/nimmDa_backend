package com.nimmda.application.promotion;

import java.util.Optional;

public interface PromotionWebhook {

    Optional<PaidCheckout> parse(String payload, String signatureHeader);
}
