package com.nimmda.application.promotion;

import com.nimmda.application.listing.ListingAuthorization;
import com.nimmda.application.listing.ListingNotFoundException;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.promotion.ListingPromotion;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartListingPromotionService {

    private final ListingRepository listingRepository;
    private final ListingPromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final PromotionCheckout promotionCheckout;
    private final String frontendBaseUrl;
    private final int priceCents;
    private final int days;

    public StartListingPromotionService(
            ListingRepository listingRepository,
            ListingPromotionRepository promotionRepository,
            UserRepository userRepository,
            PromotionCheckout promotionCheckout,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.promotion.price-cents:499}") int priceCents,
            @Value("${app.promotion.days:7}") int days
    ) {
        this.listingRepository = listingRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.promotionCheckout = promotionCheckout;
        this.frontendBaseUrl = stripTrailingSlash(frontendBaseUrl);
        this.priceCents = priceCents;
        this.days = days;
    }

    @Transactional
    public PromotionCheckoutView execute(String listingId, String userId) {
        if (!promotionCheckout.configured()) {
            throw new PromotionException(
                    "unavailable",
                    "Online-Zahlung ist noch nicht eingerichtet."
            );
        }
        Listing listing = listingRepository.findById(new ListingId(listingId))
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        ListingAuthorization.requireOwner(listing, userId);
        if (!listing.isPublished()) {
            throw new PromotionException("notActive", "Nur aktive Anzeigen können nach oben gestellt werden.");
        }
        String email = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new PromotionException("notFound", "Konto wurde nicht gefunden."))
                .email();
        ListingPromotion promotion = promotionRepository.save(
                ListingPromotion.start(listing.id(), listing.sellerId(), priceCents, "eur", days)
        );
        StartedCheckout checkout = promotionCheckout.start(new CheckoutRequest(
                promotion.id(),
                "listing",
                "Top-Anzeige für " + days + " Tage",
                "Die Anzeige „" + listing.title()
                        + "“ steht " + days
                        + " Tage über den kostenlosen Anzeigen und ist als Werbung gekennzeichnet. "
                        + formatEuro(priceCents) + ", kein Umsatzsteuerausweis.",
                email,
                priceCents,
                "eur",
                frontendBaseUrl + "/konto/meine-anzeigen?top=success&session_id={CHECKOUT_SESSION_ID}",
                frontendBaseUrl + "/konto/meine-anzeigen?top=cancel"
        ));
        promotion.attachSession(checkout.providerSessionId());
        promotionRepository.save(promotion);
        return new PromotionCheckoutView(checkout.url());
    }

    private static String formatEuro(int cents) {
        return String.format("%d,%02d €", cents / 100, cents % 100);
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:4200";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
