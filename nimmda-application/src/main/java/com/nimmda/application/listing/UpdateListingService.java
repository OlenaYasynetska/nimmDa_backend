package com.nimmda.application.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UpdateListingService implements UpdateListingUseCase {

    private final ListingRepository listingRepository;

    public UpdateListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingView execute(UpdateListingCommand command) {
        Listing listing = listingRepository
                .findById(new ListingId(command.listingId()))
                .orElseThrow(() -> new ListingNotFoundException(command.listingId()));
        ListingAuthorization.requireOwner(listing, command.actorId());
        listing.updateDetails(
                command.title(),
                Money.of(command.price()),
                new Category(command.category()),
                new Location(command.location()),
                command.imageSrc()
        );
        applyStatus(listing, command.status());
        return ListingMapper.toView(listingRepository.save(listing));
    }

    private static void applyStatus(Listing listing, String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if ("paused".equals(normalized) || "pausiert".equals(normalized)) {
            listing.pause();
        } else if ("active".equals(normalized) || "aktiv".equals(normalized)) {
            listing.activate();
        } else {
            throw new IllegalArgumentException("status must be aktiv or pausiert");
        }
    }
}
