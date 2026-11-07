package com.nimmda.application.promotion;

import com.nimmda.application.mail.SendPaymentReceiptService;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.promotion.ListingPromotion;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CompleteListingPromotionService {

    private final ListingPromotionRepository promotionRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final SendPaymentReceiptService sendPaymentReceiptService;

    public CompleteListingPromotionService(
            ListingPromotionRepository promotionRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            SendPaymentReceiptService sendPaymentReceiptService
    ) {
        this.promotionRepository = promotionRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.sendPaymentReceiptService = sendPaymentReceiptService;
    }

    @Transactional
    public ListingPromotion execute(String orderId, String providerSessionId) {
        ListingPromotion promotion = promotionRepository.findById(orderId)
                .orElseThrow(() -> new PromotionException("notFound", "Zahlung wurde nicht gefunden."));
        if (promotion.isPaid()) {
            return promotion;
        }
        if (promotion.providerSessionId() != null
                && providerSessionId != null
                && !promotion.providerSessionId().equals(providerSessionId)) {
            throw new PromotionException("invalid", "Die Zahlung gehört zu einer anderen Bestellung.");
        }
        Listing listing = listingRepository.findById(promotion.listingId())
                .orElseThrow(() -> new PromotionException("notFound", "Anzeige wurde nicht gefunden."));
        Instant now = Instant.now();
        Instant base = listing.promotedUntil() != null && listing.promotedUntil().isAfter(now)
                ? listing.promotedUntil()
                : now;
        Instant until = base.plus(Duration.ofDays(promotion.days()));
        listing.markPromotedUntil(until);
        listingRepository.save(listing);
        promotion.markPaid(now, until);
        ListingPromotion paid = promotionRepository.save(promotion);
        var buyer = userRepository.findById(paid.sellerId());
        String email = buyer.map(user -> user.email()).orElse("");
        String name = buyer.map(user -> user.firstName()).orElse("");
        sendPaymentReceiptService.send(
                email,
                name,
                "",
                "Top-Anzeige",
                listing.title(),
                paid.amountCents(),
                paid.days(),
                now,
                until,
                paid.id()
        );
        return paid;
    }
}
