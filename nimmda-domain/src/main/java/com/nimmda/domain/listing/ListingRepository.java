package com.nimmda.domain.listing;

import com.nimmda.domain.shared.UserId;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(ListingId listingId);

    List<Listing> findPublished(ListingSearch search);

    List<Listing> findBySellerId(UserId sellerId);

    List<Listing> findAll();

    void delete(ListingId listingId);
}
