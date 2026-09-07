package com.nimmda.application.listing;

public interface UpdateListingStatusUseCase {

    ListingView execute(String listingId, String sellerId, String status);
}
