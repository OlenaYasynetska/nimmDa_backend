package com.nimmda.application.business;

import com.nimmda.application.promotion.PromotionException;
import com.nimmda.domain.business.BusinessProfile;
import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BusinessProfileQuery {

    private final BusinessProfileRepository profileRepository;
    private final ListingRepository listingRepository;

    public BusinessProfileQuery(BusinessProfileRepository profileRepository, ListingRepository listingRepository) {
        this.profileRepository = profileRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public BusinessProfileView byUser(UserId userId) {
        BusinessProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new PromotionException("notFound", "Profi-Profil wurde nicht gefunden."));
        return toView(profile);
    }

    @Transactional(readOnly = true)
    public List<BusinessProfileCard> activeCards(String location) {
        return profileRepository.findActive(Instant.now(), location).stream()
                .limit(24)
                .map(profile -> new BusinessProfileCard(
                        profile.userId().value(),
                        profile.companyName(),
                        profile.trade(),
                        profile.location()
                ))
                .toList();
    }

    private BusinessProfileView toView(BusinessProfile profile) {
        Instant now = Instant.now();
        List<BusinessListingView> listings = listingRepository.findBySellerId(profile.userId()).stream()
                .filter(listing -> listing.isPublished())
                .map(listing -> new BusinessListingView(
                        listing.id().value(),
                        listing.title(),
                        listing.price().amount(),
                        listing.imageSrc(),
                        listing.category().name()
                ))
                .toList();
        return new BusinessProfileView(
                profile.userId().value(),
                profile.companyName(),
                profile.trade(),
                profile.location(),
                profile.phone(),
                profile.isActive(now),
                profile.activeUntil(),
                listings
        );
    }
}
