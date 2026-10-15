package com.nimmda.application.business;

import com.nimmda.application.mail.SendPaymentReceiptService;
import com.nimmda.application.promotion.PromotionException;
import com.nimmda.domain.user.UserRepository;
import com.nimmda.domain.business.BusinessProfile;
import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.business.BusinessSubscription;
import com.nimmda.domain.business.BusinessSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CompleteBusinessSubscriptionService {

    private final BusinessSubscriptionRepository subscriptionRepository;
    private final BusinessProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SendPaymentReceiptService sendPaymentReceiptService;

    public CompleteBusinessSubscriptionService(
            BusinessSubscriptionRepository subscriptionRepository,
            BusinessProfileRepository profileRepository,
            UserRepository userRepository,
            SendPaymentReceiptService sendPaymentReceiptService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.sendPaymentReceiptService = sendPaymentReceiptService;
    }

    @Transactional
    public BusinessSubscription execute(String orderId, String providerSessionId) {
        BusinessSubscription subscription = subscriptionRepository.findById(orderId)
                .orElseThrow(() -> new PromotionException("notFound", "Zahlung wurde nicht gefunden."));
        if (subscription.isPaid()) {
            return subscription;
        }
        if (subscription.providerSessionId() != null
                && providerSessionId != null
                && !subscription.providerSessionId().equals(providerSessionId)) {
            throw new PromotionException("invalid", "Die Zahlung gehört zu einer anderen Bestellung.");
        }
        BusinessProfile profile = profileRepository.findByUserId(subscription.userId())
                .orElseThrow(() -> new PromotionException("notFound", "Profi-Profil wurde nicht gefunden."));
        Instant now = Instant.now();
        Instant base = profile.activeUntil() != null && profile.activeUntil().isAfter(now)
                ? profile.activeUntil()
                : now;
        Instant until = base.plus(Duration.ofDays(subscription.days()));
        profile.markActiveUntil(until);
        profileRepository.save(profile);
        subscription.markPaid(now, until);
        BusinessSubscription paid = subscriptionRepository.save(subscription);
        String email = userRepository.findById(paid.userId()).map(user -> user.email()).orElse("");
        sendPaymentReceiptService.send(
                email,
                "Profi-Profil",
                profile.companyName(),
                paid.amountCents(),
                paid.days(),
                now,
                until,
                paid.id()
        );
        return paid;
    }
}
