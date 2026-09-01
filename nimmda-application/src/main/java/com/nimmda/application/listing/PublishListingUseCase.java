package com.nimmda.application.listing;

public interface PublishListingUseCase {

    ListingView execute(PublishListingCommand command);
}
