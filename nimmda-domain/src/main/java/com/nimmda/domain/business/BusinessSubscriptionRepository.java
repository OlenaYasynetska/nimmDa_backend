package com.nimmda.domain.business;

import java.util.List;
import java.util.Optional;

public interface BusinessSubscriptionRepository {

    BusinessSubscription save(BusinessSubscription subscription);

    Optional<BusinessSubscription> findById(String id);

    Optional<BusinessSubscription> findByProviderSessionId(String providerSessionId);

    List<BusinessSubscription> findPaid();
}
