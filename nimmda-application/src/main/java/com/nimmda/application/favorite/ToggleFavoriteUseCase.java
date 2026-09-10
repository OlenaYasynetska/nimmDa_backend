package com.nimmda.application.favorite;

import com.nimmda.application.listing.ListingView;

public interface ToggleFavoriteUseCase {

    ListingView execute(String userId, String listingId, boolean favorite);
}
