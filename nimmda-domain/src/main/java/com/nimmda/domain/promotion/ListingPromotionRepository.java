package com.nimmda.domain.promotion;

import java.util.List;
import java.util.Optional;

public interface ListingPromotionRepository {

    ListingPromotion save(ListingPromotion promotion);

    Optional<ListingPromotion> findById(String id);

    Optional<ListingPromotion> findByProviderSessionId(String providerSessionId);

    List<ListingPromotion> findPaid();
}
