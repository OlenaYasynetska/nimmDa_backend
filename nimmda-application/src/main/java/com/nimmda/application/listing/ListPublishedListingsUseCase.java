package com.nimmda.application.listing;

public interface ListPublishedListingsUseCase {

    PublishedListingsPage execute(PublishedListingsQuery query);
}
