package com.nimmda.infrastructure.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.ListingStatus;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListingRepositoryJpaAdapter implements ListingRepository {

    private final SpringDataListingJpaRepository jpaRepository;

    public ListingRepositoryJpaAdapter(SpringDataListingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Listing save(Listing listing) {
        return toDomain(jpaRepository.save(toEntity(listing)));
    }

    @Override
    public Optional<Listing> findById(ListingId listingId) {
        return jpaRepository.findById(listingId.value()).map(this::toDomain);
    }

    @Override
    public List<Listing> findPublished() {
        return jpaRepository.findByStatusOrderByCreatedAtDesc(ListingJpaStatus.ACTIVE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Listing> findPublishedByCategory(Category category) {
        return jpaRepository
                .findByStatusAndCategoryOrderByCreatedAtDesc(ListingJpaStatus.ACTIVE, category.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Listing> findBySellerId(UserId sellerId) {
        return jpaRepository.findBySellerIdOrderByCreatedAtDesc(sellerId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Listing> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private ListingJpaEntity toEntity(Listing listing) {
        ListingJpaEntity entity = new ListingJpaEntity();
        entity.setId(listing.id().value());
        entity.setSellerId(listing.sellerId().value());
        entity.setTitle(listing.title());
        entity.setPrice(listing.price().amount());
        entity.setCategory(listing.category().name());
        entity.setLocation(listing.location().city());
        entity.setImageSrc(listing.imageSrc());
        entity.setStatus(ListingJpaStatus.valueOf(listing.status().name()));
        entity.setViews(listing.views());
        entity.setChats(listing.chats());
        entity.setCreatedAt(listing.createdAt());
        entity.setUpdatedAt(listing.updatedAt());
        return entity;
    }

    private Listing toDomain(ListingJpaEntity entity) {
        return Listing.rehydrate(
                new ListingId(entity.getId()),
                new UserId(entity.getSellerId()),
                entity.getTitle(),
                Money.of(entity.getPrice()),
                new Category(entity.getCategory()),
                new Location(entity.getLocation()),
                entity.getImageSrc(),
                ListingStatus.valueOf(entity.getStatus().name()),
                entity.getViews(),
                entity.getChats(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
