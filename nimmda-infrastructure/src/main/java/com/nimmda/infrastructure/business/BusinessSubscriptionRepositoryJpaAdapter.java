package com.nimmda.infrastructure.business;

import com.nimmda.domain.business.BusinessSubscription;
import com.nimmda.domain.business.BusinessSubscriptionRepository;
import com.nimmda.domain.promotion.PromotionStatus;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BusinessSubscriptionRepositoryJpaAdapter implements BusinessSubscriptionRepository {

    private final SpringDataBusinessSubscriptionJpaRepository jpaRepository;

    public BusinessSubscriptionRepositoryJpaAdapter(SpringDataBusinessSubscriptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BusinessSubscription save(BusinessSubscription subscription) {
        return toDomain(jpaRepository.save(toEntity(subscription)));
    }

    @Override
    public Optional<BusinessSubscription> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<BusinessSubscription> findByProviderSessionId(String providerSessionId) {
        return jpaRepository.findByProviderSessionId(providerSessionId).map(this::toDomain);
    }

    @Override
    public List<BusinessSubscription> findPaid() {
        return jpaRepository.findByStatusOrderByPaidAtDesc(PromotionStatus.PAID).stream()
                .map(this::toDomain)
                .toList();
    }

    private BusinessSubscriptionJpaEntity toEntity(BusinessSubscription subscription) {
        BusinessSubscriptionJpaEntity entity = new BusinessSubscriptionJpaEntity();
        entity.setId(subscription.id());
        entity.setUserId(subscription.userId().value());
        entity.setAmountCents(subscription.amountCents());
        entity.setCurrency(subscription.currency());
        entity.setDays(subscription.days());
        entity.setStatus(subscription.status());
        entity.setProviderSessionId(subscription.providerSessionId());
        entity.setActiveUntil(subscription.activeUntil());
        entity.setCreatedAt(subscription.createdAt());
        entity.setPaidAt(subscription.paidAt());
        return entity;
    }

    private BusinessSubscription toDomain(BusinessSubscriptionJpaEntity entity) {
        return BusinessSubscription.rehydrate(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getAmountCents(),
                entity.getCurrency(),
                entity.getDays(),
                entity.getStatus(),
                entity.getProviderSessionId(),
                entity.getActiveUntil(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }
}
