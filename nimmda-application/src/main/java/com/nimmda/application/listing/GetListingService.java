package com.nimmda.application.listing;

import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetListingService implements GetListingUseCase {

    private final ListingRepository listingRepository;

    public GetListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingView execute(String listingId) {
        Listing listing = listingRepository
                .findById(new ListingId(listingId))
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!listing.isPublished()) {
            throw new ListingNotFoundException(listingId);
        }
        listing.recordView();
        return ListingMapper.toView(listingRepository.save(listing));
    }
}
