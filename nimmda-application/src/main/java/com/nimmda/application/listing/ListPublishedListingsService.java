package com.nimmda.application.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListPublishedListingsService implements ListPublishedListingsUseCase {

    private final ListingRepository listingRepository;

    public ListPublishedListingsService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingView> execute(String category) {
        if (category == null || category.isBlank()) {
            return listingRepository.findPublished().stream().map(ListingMapper::toView).toList();
        }
        return listingRepository.findPublishedByCategory(new Category(category)).stream()
                .map(ListingMapper::toView)
                .toList();
    }
}
