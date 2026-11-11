package com.nimmda.infrastructure.promotion;

import com.nimmda.domain.promotion.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataListingPromotionJpaRepository extends JpaRepository<ListingPromotionJpaEntity, String> {

    Optional<ListingPromotionJpaEntity> findByProviderSessionId(String providerSessionId);

    List<ListingPromotionJpaEntity> findByStatusOrderByPaidAtDesc(PromotionStatus status);

    List<ListingPromotionJpaEntity> findBySellerIdAndStatus(String sellerId, PromotionStatus status);
}
