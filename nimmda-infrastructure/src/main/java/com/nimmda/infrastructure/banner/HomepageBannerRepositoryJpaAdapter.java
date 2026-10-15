package com.nimmda.infrastructure.banner;

import com.nimmda.domain.banner.HomepageBanner;
import com.nimmda.domain.banner.HomepageBannerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class HomepageBannerRepositoryJpaAdapter implements HomepageBannerRepository {

    private final SpringDataHomepageBannerJpaRepository jpaRepository;

    public HomepageBannerRepositoryJpaAdapter(SpringDataHomepageBannerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HomepageBanner save(HomepageBanner banner) {
        return toDomain(jpaRepository.save(toEntity(banner)));
    }

    @Override
    public Optional<HomepageBanner> find() {
        return jpaRepository.findById(HomepageBanner.ID).map(this::toDomain);
    }

    private HomepageBannerJpaEntity toEntity(HomepageBanner banner) {
        HomepageBannerJpaEntity entity = new HomepageBannerJpaEntity();
        entity.setId(HomepageBanner.ID);
        entity.setAdvertiser(banner.advertiser());
        entity.setImageSrc(banner.imageSrc());
        entity.setTargetUrl(banner.targetUrl());
        entity.setStartsOn(banner.startsOn());
        entity.setEndsOn(banner.endsOn());
        entity.setUpdatedAt(banner.updatedAt());
        return entity;
    }

    private HomepageBanner toDomain(HomepageBannerJpaEntity entity) {
        return new HomepageBanner(
                entity.getAdvertiser(),
                entity.getImageSrc(),
                entity.getTargetUrl(),
                entity.getStartsOn(),
                entity.getEndsOn(),
                entity.getUpdatedAt()
        );
    }
}
