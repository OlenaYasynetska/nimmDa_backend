package com.nimmda.application.promotion;

import com.nimmda.domain.promotion.ListingPromotion;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SettlePendingPromotionsService {

    private static final Logger log = Logger.getLogger(SettlePendingPromotionsService.class.getName());

    private final ListingPromotionRepository promotionRepository;
    private final PromotionCheckout promotionCheckout;
    private final CompleteListingPromotionService completeListingPromotionService;

    public SettlePendingPromotionsService(
            ListingPromotionRepository promotionRepository,
            PromotionCheckout promotionCheckout,
            CompleteListingPromotionService completeListingPromotionService
    ) {
        this.promotionRepository = promotionRepository;
        this.promotionCheckout = promotionCheckout;
        this.completeListingPromotionService = completeListingPromotionService;
    }

    public void execute(String userId) {
        if (!promotionCheckout.configured() || userId == null || userId.isBlank()) {
            return;
        }
        for (ListingPromotion promotion : promotionRepository.findPendingBySeller(new UserId(userId))) {
            String sessionId = promotion.providerSessionId();
            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }
            try {
                PaidCheckout remote = promotionCheckout.retrieve(sessionId);
                if (!remote.paid()) {
                    continue;
                }
                if (remote.orderId() != null && !remote.orderId().equals(promotion.id())) {
                    continue;
                }
                completeListingPromotionService.execute(
                        promotion.id(),
                        remote.providerSessionId() == null ? sessionId : remote.providerSessionId()
                );
            } catch (RuntimeException ex) {
                log.warning("Pending promotion " + promotion.id() + " was not applied: " + ex.getMessage());
            }
        }
    }
}
