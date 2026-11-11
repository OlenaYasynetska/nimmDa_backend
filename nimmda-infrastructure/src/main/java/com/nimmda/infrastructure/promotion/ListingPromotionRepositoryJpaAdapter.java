package com.nimmda.infrastructure.promotion;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.promotion.ListingPromotion;
import com.nimmda.domain.promotion.ListingPromotionRepository;
import com.nimmda.domain.promotion.PromotionStatus;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListingPromotionRepositoryJpaAdapter implements ListingPromotionRepository {

    private final SpringDataListingPromotionJpaRepository jpaRepository;

    public ListingPromotionRepositoryJpaAdapter(SpringDataListingPromotionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ListingPromotion save(ListingPromotion promotion) {
        return toDomain(jpaRepository.save(toEntity(promotion)));
    }

    @Override
    public Optional<ListingPromotion> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ListingPromotion> findByProviderSessionId(String providerSessionId) {
        return jpaRepository.findByProviderSessionId(providerSessionId).map(this::toDomain);
    }

    @Override
    public List<ListingPromotion> findPaid() {
        return jpaRepository.findByStatusOrderByPaidAtDesc(PromotionStatus.PAID).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ListingPromotion> findPendingBySeller(UserId sellerId) {
        return jpaRepository.findBySellerIdAndStatus(sellerId.value(), PromotionStatus.PENDING).stream()
                .map(this::toDomain)
                .toList();
    }

    private ListingPromotionJpaEntity toEntity(ListingPromotion promotion) {
        ListingPromotionJpaEntity entity = new ListingPromotionJpaEntity();
        entity.setId(promotion.id());
        entity.setListingId(promotion.listingId().value());
        entity.setSellerId(promotion.sellerId().value());
        entity.setAmountCents(promotion.amountCents());
        entity.setCurrency(promotion.currency());
        entity.setDays(promotion.days());
        entity.setStatus(promotion.status());
        entity.setProviderSessionId(promotion.providerSessionId());
        entity.setPromotedUntil(promotion.promotedUntil());
        entity.setCreatedAt(promotion.createdAt());
        entity.setPaidAt(promotion.paidAt());
        return entity;
    }

    private ListingPromotion toDomain(ListingPromotionJpaEntity entity) {
        return ListingPromotion.rehydrate(
                entity.getId(),
                new ListingId(entity.getListingId()),
                new UserId(entity.getSellerId()),
                entity.getAmountCents(),
                entity.getCurrency(),
                entity.getDays(),
                entity.getStatus(),
                entity.getProviderSessionId(),
                entity.getPromotedUntil(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }
}
