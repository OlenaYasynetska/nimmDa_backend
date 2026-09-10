package com.nimmda.application.favorite;

import com.nimmda.application.listing.ListingMapper;
import com.nimmda.application.listing.ListingView;
import com.nimmda.domain.favorite.FavoriteRepository;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ListFavoritesService implements ListFavoritesUseCase {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public ListFavoritesService(FavoriteRepository favoriteRepository, ListingRepository listingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingView> execute(String userId) {
        return favoriteRepository.findListingIdsByUserId(new UserId(userId)).stream()
                .map(listingRepository::findById)
                .map(item -> item.orElse(null))
                .filter(Objects::nonNull)
                .filter(Listing::isPublished)
                .map(ListingMapper::toView)
                .toList();
    }
}
