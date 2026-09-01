package com.nimmda.application.listing;

public class ListingNotFoundException extends RuntimeException {

    public ListingNotFoundException(String listingId) {
        super("Listing not found: " + listingId);
    }
}
