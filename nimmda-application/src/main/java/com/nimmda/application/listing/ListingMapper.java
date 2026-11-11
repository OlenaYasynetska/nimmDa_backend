package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;

import java.time.Instant;
import java.util.List;

public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingView toView(Listing listing) {
        Instant now = Instant.now();
        List<String> images = ListingPhotoAddresses.forResponse(listing);
        String cover = images.isEmpty() ? listing.imageSrc() : images.get(0);
        return new ListingView(
                listing.id().value(),
                listing.sellerId().value(),
                listing.title(),
                listing.price().amount(),
                listing.category().name(),
                listing.location().city(),
                listing.description(),
                cover,
                images,
                listing.status().name(),
                listing.views(),
                listing.chats(),
                listing.createdAt(),
                listing.promotedUntil(),
                listing.isPromoted(now)
        );
    }
}
