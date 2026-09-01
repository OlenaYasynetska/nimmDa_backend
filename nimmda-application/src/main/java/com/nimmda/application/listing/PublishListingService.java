package com.nimmda.application.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishListingService implements PublishListingUseCase {

    private final ListingRepository listingRepository;

    public PublishListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingView execute(PublishListingCommand command) {
        Listing listing = Listing.publishNew(
                new UserId(command.sellerId()),
                command.title(),
                Money.of(command.price()),
                new Category(command.category()),
                new Location(command.location()),
                command.imageSrc()
        );
        return ListingMapper.toView(listingRepository.save(listing));
    }
}
