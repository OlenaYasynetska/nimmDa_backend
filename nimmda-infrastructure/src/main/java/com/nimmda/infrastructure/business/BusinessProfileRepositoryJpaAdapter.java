package com.nimmda.infrastructure.business;

import com.nimmda.domain.business.BusinessProfile;
import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BusinessProfileRepositoryJpaAdapter implements BusinessProfileRepository {

    private final SpringDataBusinessProfileJpaRepository jpaRepository;

    public BusinessProfileRepositoryJpaAdapter(SpringDataBusinessProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BusinessProfile save(BusinessProfile profile) {
        return toDomain(jpaRepository.save(toEntity(profile)));
    }

    @Override
    public Optional<BusinessProfile> findByUserId(UserId userId) {
        return jpaRepository.findById(userId.value()).map(this::toDomain);
    }

    @Override
    public List<BusinessProfile> findActive(Instant now, String location) {
        String normalized = location == null || location.isBlank() ? null : location.trim().toLowerCase();
        List<BusinessProfileJpaEntity> rows = normalized == null
                ? jpaRepository.findByActiveUntilAfterOrderByCompanyNameAsc(now)
                : jpaRepository.findActiveInLocation(now, normalized);
        return rows.stream().map(this::toDomain).toList();
    }

    private BusinessProfileJpaEntity toEntity(BusinessProfile profile) {
        BusinessProfileJpaEntity entity = new BusinessProfileJpaEntity();
        entity.setUserId(profile.userId().value());
        entity.setCompanyName(profile.companyName());
        entity.setTrade(profile.trade());
        entity.setLocation(profile.location());
        entity.setPhone(profile.phone());
        entity.setActiveUntil(profile.activeUntil());
        entity.setCreatedAt(profile.createdAt());
        entity.setUpdatedAt(profile.updatedAt());
        return entity;
    }

    private BusinessProfile toDomain(BusinessProfileJpaEntity entity) {
        return BusinessProfile.rehydrate(
                new UserId(entity.getUserId()),
                entity.getCompanyName(),
                entity.getTrade(),
                entity.getLocation(),
                entity.getPhone(),
                entity.getActiveUntil(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
