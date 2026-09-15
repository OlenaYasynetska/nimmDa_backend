package com.nimmda.application.listing;

import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.ListingSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublishedListingsService implements ListPublishedListingsUseCase {

    private final ListingRepository listingRepository;

    public ListPublishedListingsService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PublishedListingsPage execute(PublishedListingsQuery query) {
        PublishedListingsQuery safe = query == null
                ? new PublishedListingsQuery(null, null, null, null, null, null, false, 0, PublishedListingsQuery.DEFAULT_SIZE)
                : query;
        ListingSearchResult result = listingRepository.findPublished(safe.toSearch(), safe.page(), safe.size());
        return new PublishedListingsPage(
                result.content().stream().map(ListingMapper::toView).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
