package com.nimmda.application.promotion;

import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListPromotionPaymentsService {

    private final ListingPromotionRepository promotionRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListPromotionPaymentsService(
            ListingPromotionRepository promotionRepository,
            ListingRepository listingRepository,
            UserRepository userRepository
    ) {
        this.promotionRepository = promotionRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PromotionPaymentView> execute() {
        return promotionRepository.findPaid().stream()
                .map(promotion -> new PromotionPaymentView(
                        promotion.id(),
                        promotion.listingId().value(),
                        listingRepository.findById(promotion.listingId())
                                .map(listing -> listing.title())
                                .orElse("Anzeige"),
                        userRepository.findById(promotion.sellerId())
                                .map(user -> user.email())
                                .orElse(""),
                        promotion.amountCents(),
                        promotion.currency(),
                        promotion.paidAt(),
                        promotion.promotedUntil()
                ))
                .toList();
    }
}
