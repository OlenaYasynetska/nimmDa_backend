package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;

import java.time.Instant;

public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingView toView(Listing listing) {
        Instant now = Instant.now();
        return new ListingView(
                listing.id().value(),
                listing.sellerId().value(),
                listing.title(),
                listing.price().amount(),
                listing.category().name(),
                listing.location().city(),
                listing.description(),
                listing.imageSrc(),
                listing.images(),
                listing.status().name(),
                listing.views(),
                listing.chats(),
                listing.createdAt(),
                listing.promotedUntil(),
                listing.isPromoted(now)
        );
    }
}
