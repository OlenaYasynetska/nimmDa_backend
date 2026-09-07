package com.nimmda.application.listing;

import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UpdateListingStatusService implements UpdateListingStatusUseCase {

    private final ListingRepository listingRepository;

    public UpdateListingStatusService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingView execute(String listingId, String sellerId, String status) {
        Listing listing = listingRepository
                .findById(new ListingId(listingId))
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!listing.sellerId().value().equals(sellerId)) {
            throw new ForbiddenActionException("Not the listing owner");
        }
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        if ("paused".equals(normalized) || "pausiert".equals(normalized)) {
            listing.pause();
        } else if ("active".equals(normalized) || "aktiv".equals(normalized)) {
            listing.activate();
        } else {
            throw new IllegalArgumentException("status must be aktiv or pausiert");
        }
        return ListingMapper.toView(listingRepository.save(listing));
    }
}
