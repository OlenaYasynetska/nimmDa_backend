package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteListingService implements DeleteListingUseCase {

    private final ListingRepository listingRepository;

    public DeleteListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public void execute(String listingId, String actorId) {
        Listing listing = listingRepository
                .findById(new ListingId(listingId))
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        ListingAuthorization.requireOwner(listing, actorId);
        listingRepository.delete(listing.id());
    }
}
