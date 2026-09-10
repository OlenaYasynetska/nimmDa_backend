package com.nimmda.application.favorite;

import com.nimmda.application.listing.ListingMapper;
import com.nimmda.application.listing.ListingNotFoundException;
import com.nimmda.application.listing.ListingView;
import com.nimmda.domain.favorite.FavoriteRepository;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ToggleFavoriteService implements ToggleFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public ToggleFavoriteService(FavoriteRepository favoriteRepository, ListingRepository listingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ListingView execute(String userId, String listingId, boolean favorite) {
        Listing listing = listingRepository
                .findById(new ListingId(listingId))
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!listing.isPublished()) {
            throw new ListingNotFoundException(listingId);
        }
        UserId user = new UserId(userId);
        if (favorite) {
            favoriteRepository.add(user, listing.id());
        } else {
            favoriteRepository.remove(user, listing.id());
        }
        return ListingMapper.toView(listing);
    }
}
