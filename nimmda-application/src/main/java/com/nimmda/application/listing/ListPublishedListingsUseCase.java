package com.nimmda.application.listing;

import java.util.List;

public interface ListPublishedListingsUseCase {

    List<ListingView> execute(String category);
}
