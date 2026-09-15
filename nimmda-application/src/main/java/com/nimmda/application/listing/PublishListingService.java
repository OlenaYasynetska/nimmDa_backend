package com.nimmda.application.listing;

import com.nimmda.application.place.ResolvePlaceService;
import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import com.nimmda.domain.place.Place;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishListingService implements PublishListingUseCase {

    private static final String DEFAULT_IMAGE = "/assets/images/Furniture.png";

    private final ListingRepository listingRepository;
    private final ResolvePlaceService resolvePlaceService;

    public PublishListingService(ListingRepository listingRepository, ResolvePlaceService resolvePlaceService) {
        this.listingRepository = listingRepository;
        this.resolvePlaceService = resolvePlaceService;
    }

    @Override
    @Transactional
    public ListingView execute(PublishListingCommand command) {
        Place place = resolvePlaceService.requireForListing(command.location());
        Listing listing = Listing.publishNew(
                new UserId(command.sellerId()),
                command.title(),
                Money.of(command.price()),
                new Category(command.category()),
                new Location(place.name(), place.coordinates()),
                blankToDefault(command.imageSrc(), DEFAULT_IMAGE)
        );
        return ListingMapper.toView(listingRepository.save(listing));
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
