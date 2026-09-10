package com.nimmda.application.listing;

import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.listing.Listing;

public final class ListingAuthorization {

    private ListingAuthorization() {
    }

    public static Listing requireOwner(Listing listing, String userId) {
        if (!listing.sellerId().value().equals(userId)) {
            throw new ForbiddenActionException("Not the listing owner");
        }
        return listing;
    }
}
