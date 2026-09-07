package com.nimmda.application.listing;

import java.util.List;

public interface ListSellerListingsUseCase {

    List<ListingView> execute(String sellerId);
}
