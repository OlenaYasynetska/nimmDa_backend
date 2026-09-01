package com.nimmda.infrastructure.listing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataListingJpaRepository extends JpaRepository<ListingJpaEntity, String> {

    List<ListingJpaEntity> findByStatusOrderByCreatedAtDesc(ListingJpaStatus status);

    List<ListingJpaEntity> findByStatusAndCategoryOrderByCreatedAtDesc(ListingJpaStatus status, String category);

    List<ListingJpaEntity> findBySellerIdOrderByCreatedAtDesc(String sellerId);
}
