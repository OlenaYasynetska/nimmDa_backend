package com.nimmda.infrastructure.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.ListingSearch;
import com.nimmda.domain.listing.ListingSort;
import com.nimmda.domain.listing.ListingStatus;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import com.nimmda.domain.shared.UserId;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    public List<Listing> findPublished(ListingSearch search) {
        ListingSearch criteria = search == null ? ListingSearch.allPublished() : search;
        return jpaRepository.findAll(publishedSpec(criteria), sortOf(criteria.sort())).stream()
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

    @Override
    public void delete(ListingId listingId) {
        jpaRepository.deleteById(listingId.value());
    }

    private static Specification<ListingJpaEntity> publishedSpec(ListingSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), ListingJpaStatus.ACTIVE));
            String like = search.textLike();
            if (like != null) {
                predicates.add(cb.like(cb.lower(root.get("title")), like, '\\'));
            }
            if (search.category() != null) {
                predicates.add(cb.equal(root.get("category"), search.category()));
            }
            if (search.location() != null) {
                predicates.add(cb.equal(cb.lower(root.get("location")), search.location().toLowerCase(Locale.ROOT)));
            }
            if (search.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), search.minPrice()));
            }
            if (search.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), search.maxPrice()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Sort sortOf(ListingSort sort) {
        ListingSort safe = sort == null ? ListingSort.NEWEST : sort;
        return switch (safe) {
            case PRICE_ASC -> Sort.by(Sort.Order.asc("price"), Sort.Order.desc("createdAt"));
            case PRICE_DESC -> Sort.by(Sort.Order.desc("price"), Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NEWEST -> Sort.by(Sort.Order.desc("createdAt"));
        };
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
