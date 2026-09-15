package com.nimmda.application.listing;

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
    public List<ListingView> execute(PublishedListingsQuery query) {
        PublishedListingsQuery safe = query == null
                ? new PublishedListingsQuery(null, null, null, null, null, null, false)
                : query;
        return listingRepository.findPublished(safe.toSearch()).stream()
                .map(ListingMapper::toView)
                .toList();
    }
}
