package com.nimmda.domain.listing;

import com.nimmda.domain.shared.UserId;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(ListingId listingId);

    List<Listing> findPublished();

    List<Listing> findPublishedByCategory(Category category);

    List<Listing> findBySellerId(UserId sellerId);
}
