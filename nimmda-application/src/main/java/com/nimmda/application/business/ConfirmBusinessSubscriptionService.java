package com.nimmda.application.business;

import com.nimmda.application.promotion.PaidCheckout;
import com.nimmda.application.promotion.PromotionCheckout;
import com.nimmda.application.promotion.PromotionException;
import com.nimmda.application.promotion.PromotionStatusView;
import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.business.BusinessSubscription;
import com.nimmda.domain.business.BusinessSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmBusinessSubscriptionService {

    private final BusinessSubscriptionRepository subscriptionRepository;
    private final PromotionCheckout promotionCheckout;
    private final CompleteBusinessSubscriptionService completeBusinessSubscriptionService;

    public ConfirmBusinessSubscriptionService(
            BusinessSubscriptionRepository subscriptionRepository,
            PromotionCheckout promotionCheckout,
            CompleteBusinessSubscriptionService completeBusinessSubscriptionService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.promotionCheckout = promotionCheckout;
        this.completeBusinessSubscriptionService = completeBusinessSubscriptionService;
    }

    @Transactional
    public PromotionStatusView execute(String userId, String providerSessionId) {
        if (providerSessionId == null || providerSessionId.isBlank()) {
            throw new PromotionException("invalid", "Die Zahlungssitzung fehlt.");
        }
        BusinessSubscription subscription = subscriptionRepository.findByProviderSessionId(providerSessionId.trim())
                .orElseThrow(() -> new PromotionException("notFound", "Zahlung wurde nicht gefunden."));
        if (!subscription.userId().value().equals(userId)) {
            throw new ForbiddenActionException("Not the profile owner");
        }
        if (subscription.isPaid()) {
            return new PromotionStatusView("paid", subscription.activeUntil());
        }
        PaidCheckout remote = promotionCheckout.retrieve(providerSessionId.trim());
        if (remote.orderId() != null && !remote.orderId().equals(subscription.id())) {
            throw new PromotionException("invalid", "Die Zahlung gehört zu einer anderen Bestellung.");
        }
        if (remote.kind() != null && !remote.kind().isBlank() && !"business".equals(remote.kind())) {
            throw new PromotionException("invalid", "Die Zahlung gehört zu einer anderen Bestellung.");
        }
        if (!remote.paid()) {
            return new PromotionStatusView("pending", null);
        }
        BusinessSubscription paid = completeBusinessSubscriptionService.execute(subscription.id(), remote.providerSessionId());
        return new PromotionStatusView("paid", paid.activeUntil());
    }
}
