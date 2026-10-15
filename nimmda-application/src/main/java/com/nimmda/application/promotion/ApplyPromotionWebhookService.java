package com.nimmda.application.promotion;

import com.nimmda.application.business.CompleteBusinessSubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class ApplyPromotionWebhookService {

    private final PromotionWebhook promotionWebhook;
    private final CompleteListingPromotionService completeListingPromotionService;
    private final CompleteBusinessSubscriptionService completeBusinessSubscriptionService;

    public ApplyPromotionWebhookService(
            PromotionWebhook promotionWebhook,
            CompleteListingPromotionService completeListingPromotionService,
            CompleteBusinessSubscriptionService completeBusinessSubscriptionService
    ) {
        this.promotionWebhook = promotionWebhook;
        this.completeListingPromotionService = completeListingPromotionService;
        this.completeBusinessSubscriptionService = completeBusinessSubscriptionService;
    }

    public void execute(String payload, String signatureHeader) {
        promotionWebhook.parse(payload, signatureHeader).ifPresent(paid -> {
            if ("business".equals(paid.kind())) {
                completeBusinessSubscriptionService.execute(paid.orderId(), paid.providerSessionId());
                return;
            }
            completeListingPromotionService.execute(paid.orderId(), paid.providerSessionId());
        });
    }
}
