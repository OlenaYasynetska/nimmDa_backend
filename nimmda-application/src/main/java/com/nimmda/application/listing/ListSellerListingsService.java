package com.nimmda.application.listing;

import com.nimmda.application.promotion.SettlePendingPromotionsService;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSellerListingsService implements ListSellerListingsUseCase {

    private final ListingRepository listingRepository;
    private final SettlePendingPromotionsService settlePendingPromotionsService;

    public ListSellerListingsService(
            ListingRepository listingRepository,
            SettlePendingPromotionsService settlePendingPromotionsService
    ) {
        this.listingRepository = listingRepository;
        this.settlePendingPromotionsService = settlePendingPromotionsService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingView> execute(String sellerId) {
        settlePendingPromotionsService.execute(sellerId);
        return listingRepository.findBySellerId(new UserId(sellerId)).stream()
                .map(ListingMapper::toView)
                .toList();
    }
}
