package com.nimmda.application.business;

import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.business.BusinessSubscriptionRepository;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListBusinessSubscriptionsService {

    private final BusinessSubscriptionRepository subscriptionRepository;
    private final BusinessProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ListBusinessSubscriptionsService(
            BusinessSubscriptionRepository subscriptionRepository,
            BusinessProfileRepository profileRepository,
            UserRepository userRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessSubscriptionView> execute() {
        return subscriptionRepository.findPaid().stream()
                .map(subscription -> new BusinessSubscriptionView(
                        subscription.id(),
                        subscription.userId().value(),
                        profileRepository.findByUserId(subscription.userId())
                                .map(profile -> profile.companyName())
                                .orElse("Firma"),
                        userRepository.findById(subscription.userId())
                                .map(user -> user.email())
                                .orElse(""),
                        subscription.amountCents(),
                        subscription.currency(),
                        subscription.paidAt(),
                        subscription.activeUntil()
                ))
                .toList();
    }
}
