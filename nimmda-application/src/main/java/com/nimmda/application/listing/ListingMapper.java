package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;

public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingView toView(Listing listing) {
        return new ListingView(
                listing.id().value(),
                listing.sellerId().value(),
                listing.title(),
                listing.price().amount(),
                listing.category().name(),
                listing.location().city(),
                listing.imageSrc(),
                listing.status().name(),
                listing.views(),
                listing.chats(),
                listing.createdAt()
        );
    }
}
