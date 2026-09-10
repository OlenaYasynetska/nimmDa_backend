package com.nimmda.infrastructure.favorite;

import com.nimmda.domain.favorite.FavoriteRepository;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class FavoriteRepositoryJpaAdapter implements FavoriteRepository {

    private final SpringDataFavoriteJpaRepository jpaRepository;

    public FavoriteRepositoryJpaAdapter(SpringDataFavoriteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void add(UserId userId, ListingId listingId) {
        if (jpaRepository.existsByUserIdAndListingId(userId.value(), listingId.value())) {
            return;
        }
        FavoriteJpaEntity entity = new FavoriteJpaEntity();
        entity.setUserId(userId.value());
        entity.setListingId(listingId.value());
        entity.setCreatedAt(Instant.now());
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void remove(UserId userId, ListingId listingId) {
        jpaRepository.deleteByUserIdAndListingId(userId.value(), listingId.value());
    }

    @Override
    public boolean exists(UserId userId, ListingId listingId) {
        return jpaRepository.existsByUserIdAndListingId(userId.value(), listingId.value());
    }

    @Override
    public List<ListingId> findListingIdsByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.value()).stream()
                .map(item -> new ListingId(item.getListingId()))
                .toList();
    }
}
