package com.nimmda.infrastructure.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SpringDataListingJpaRepository
        extends JpaRepository<ListingJpaEntity, String>, JpaSpecificationExecutor<ListingJpaEntity> {

    List<ListingJpaEntity> findBySellerIdOrderByCreatedAtDesc(String sellerId);
}
