package com.nimmda.application.favorite;

import com.nimmda.application.listing.ListingView;

import java.util.List;

public interface ListFavoritesUseCase {

    List<ListingView> execute(String userId);
}
