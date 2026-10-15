package com.nimmda.infrastructure.business;

import com.nimmda.domain.promotion.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBusinessSubscriptionJpaRepository extends JpaRepository<BusinessSubscriptionJpaEntity, String> {

    Optional<BusinessSubscriptionJpaEntity> findByProviderSessionId(String providerSessionId);

    List<BusinessSubscriptionJpaEntity> findByStatusOrderByPaidAtDesc(PromotionStatus status);
}
