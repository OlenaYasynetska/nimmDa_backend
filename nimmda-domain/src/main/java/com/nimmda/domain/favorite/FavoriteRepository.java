package com.nimmda.domain.favorite;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.shared.UserId;

import java.util.List;

public interface FavoriteRepository {

    void add(UserId userId, ListingId listingId);

    void remove(UserId userId, ListingId listingId);

    boolean exists(UserId userId, ListingId listingId);

    List<ListingId> findListingIdsByUserId(UserId userId);
}
