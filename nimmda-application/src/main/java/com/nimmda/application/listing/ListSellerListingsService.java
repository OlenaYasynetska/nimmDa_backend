package com.nimmda.application.listing;

import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSellerListingsService implements ListSellerListingsUseCase {

    private final ListingRepository listingRepository;

    public ListSellerListingsService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingView> execute(String sellerId) {
        return listingRepository.findBySellerId(new UserId(sellerId)).stream()
                .map(ListingMapper::toView)
                .toList();
    }
}
