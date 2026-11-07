package com.nimmda.application.business;

import com.nimmda.application.promotion.CheckoutRequest;
import com.nimmda.application.promotion.PromotionCheckout;
import com.nimmda.application.promotion.PromotionCheckoutView;
import com.nimmda.application.promotion.PromotionException;
import com.nimmda.application.promotion.StartedCheckout;
import com.nimmda.domain.business.BusinessProfile;
import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.business.BusinessSubscription;
import com.nimmda.domain.business.BusinessSubscriptionRepository;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartBusinessSubscriptionService {

    private final BusinessProfileRepository profileRepository;
    private final BusinessSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PromotionCheckout promotionCheckout;
    private final String frontendBaseUrl;
    private final int priceCents;
    private final int days;

    public StartBusinessSubscriptionService(
            BusinessProfileRepository profileRepository,
            BusinessSubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            PromotionCheckout promotionCheckout,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.business.price-cents:1999}") int priceCents,
            @Value("${app.business.days:30}") int days
    ) {
        this.profileRepository = profileRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.promotionCheckout = promotionCheckout;
        this.frontendBaseUrl = stripTrailingSlash(frontendBaseUrl);
        this.priceCents = priceCents;
        this.days = days;
    }

    @Transactional
    public PromotionCheckoutView execute(String userId) {
        if (!promotionCheckout.configured()) {
            throw new PromotionException("unavailable", "Online-Zahlung ist noch nicht eingerichtet.");
        }
        UserId ownerId = new UserId(userId);
        BusinessProfile profile = profileRepository.findByUserId(ownerId)
                .orElseThrow(() -> new PromotionException("notFound", "Lege zuerst das Profi-Profil an."));
        String email = userRepository.findById(ownerId)
                .orElseThrow(() -> new PromotionException("notFound", "Konto wurde nicht gefunden."))
                .email();
        BusinessSubscription subscription = subscriptionRepository.save(
                BusinessSubscription.start(ownerId, priceCents, "eur", days)
        );
        String priceLabel = formatEuro(priceCents);
        StartedCheckout checkout = promotionCheckout.start(new CheckoutRequest(
                subscription.id(),
                "business",
                "Profi-Profil für " + days + " Tage",
                "Firmenprofil von " + profile.companyName()
                        + " steht " + days
                        + " Tage über privaten Anzeigen in Services und ist als Firma gekennzeichnet. "
                        + priceLabel + ", kein Umsatzsteuerausweis.",
                email,
                priceCents,
                "eur",
                frontendBaseUrl + "/konto/profi?profi=success&session_id={CHECKOUT_SESSION_ID}",
                frontendBaseUrl + "/konto/profi?profi=cancel"
        ));
        subscription.attachSession(checkout.providerSessionId());
        subscriptionRepository.save(subscription);
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
