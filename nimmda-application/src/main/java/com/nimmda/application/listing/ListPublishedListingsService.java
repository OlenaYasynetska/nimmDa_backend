package com.nimmda.application.listing;

import com.nimmda.application.place.ResolvePlaceService;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.ListingSearchResult;
import com.nimmda.domain.listing.ListingSort;
import com.nimmda.domain.place.Place;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublishedListingsService implements ListPublishedListingsUseCase {

    private final ListingRepository listingRepository;
    private final ResolvePlaceService resolvePlaceService;

    public ListPublishedListingsService(
            ListingRepository listingRepository,
            ResolvePlaceService resolvePlaceService
    ) {
        this.listingRepository = listingRepository;
        this.resolvePlaceService = resolvePlaceService;
    }

    @Override
    @Transactional
    public PublishedListingsPage execute(PublishedListingsQuery query) {
        PublishedListingsQuery safe = query == null
                ? new PublishedListingsQuery(null, null, null, null, null, null, false, null, 0, PublishedListingsQuery.DEFAULT_SIZE)
                : query;
        PublishedListingsQuery resolved = withOrigin(safe);
        ListingSearchResult result = listingRepository.findPublished(resolved.toSearch(), resolved.page(), resolved.size());
        return new PublishedListingsPage(
                result.content().stream().map(ListingMapper::toView).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private PublishedListingsQuery withOrigin(PublishedListingsQuery query) {
        if (query.location() == null || query.location().isBlank()) {
            return query;
        }
        boolean needsOrigin = query.km() != null || ListingSort.from(query.sort()) == ListingSort.DISTANCE;
        if (!needsOrigin) {
            return query;
        }
        return resolvePlaceService.findOrigin(query.location())
                .map(Place::coordinates)
                .map(query::withOrigin)
                .orElse(query);
    }
}
