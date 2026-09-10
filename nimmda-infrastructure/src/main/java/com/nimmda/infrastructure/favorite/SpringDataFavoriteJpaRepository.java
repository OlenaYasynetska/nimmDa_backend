package com.nimmda.infrastructure.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataFavoriteJpaRepository extends JpaRepository<FavoriteJpaEntity, FavoriteEntityId> {

    List<FavoriteJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByUserIdAndListingId(String userId, String listingId);

    void deleteByUserIdAndListingId(String userId, String listingId);
}
