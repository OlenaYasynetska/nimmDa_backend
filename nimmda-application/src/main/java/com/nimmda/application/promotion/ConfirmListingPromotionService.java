package com.nimmda.application.promotion;

import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.promotion.ListingPromotion;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmListingPromotionService {

    private final ListingPromotionRepository promotionRepository;
    private final PromotionCheckout promotionCheckout;
    private final CompleteListingPromotionService completeListingPromotionService;

    public ConfirmListingPromotionService(
            ListingPromotionRepository promotionRepository,
            PromotionCheckout promotionCheckout,
            CompleteListingPromotionService completeListingPromotionService
    ) {
        this.promotionRepository = promotionRepository;
        this.promotionCheckout = promotionCheckout;
        this.completeListingPromotionService = completeListingPromotionService;
    }

    @Transactional
    public PromotionStatusView execute(String userId, String providerSessionId) {
        if (providerSessionId == null || providerSessionId.isBlank()) {
            throw new PromotionException("invalid", "Die Zahlungssitzung fehlt.");
        }
        ListingPromotion promotion = promotionRepository.findByProviderSessionId(providerSessionId.trim())
                .orElseThrow(() -> new PromotionException("notFound", "Zahlung wurde nicht gefunden."));
        if (!promotion.sellerId().value().equals(userId)) {
            throw new ForbiddenActionException("Not the listing owner");
        }
        if (promotion.isPaid()) {
            return new PromotionStatusView("paid", promotion.promotedUntil());
        }
        PaidCheckout remote = promotionCheckout.retrieve(providerSessionId.trim());
        if (remote.orderId() != null && !remote.orderId().equals(promotion.id())) {
            throw new PromotionException("invalid", "Die Zahlung gehört zu einer anderen Bestellung.");
        }
        if (!remote.paid()) {
            return new PromotionStatusView("pending", null);
        }
        ListingPromotion paid = completeListingPromotionService.execute(promotion.id(), remote.providerSessionId());
        return new PromotionStatusView("paid", paid.promotedUntil());
    }
}
