package com.nimmda.infrastructure.listing;

import com.nimmda.domain.listing.Category;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.listing.ListingSearch;
import com.nimmda.domain.listing.ListingSearchResult;
import com.nimmda.domain.listing.ListingSort;
import com.nimmda.domain.listing.ListingStatus;
import com.nimmda.domain.listing.Location;
import com.nimmda.domain.listing.Money;
import com.nimmda.domain.geo.GeoCoordinates;
import com.nimmda.domain.shared.UserId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
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
    public ListingSearchResult findPublished(ListingSearch search, int page, int size) {
        ListingSearch criteria = search == null ? ListingSearch.allPublished() : search;
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : size;
        var result = jpaRepository.findAll(
                publishedSpec(criteria),
                PageRequest.of(safePage, safeSize, Sort.unsorted())
        );
        return new ListingSearchResult(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
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
            Expression<Double> distanceKm = null;
            if (search.origin() != null) {
                predicates.add(cb.isNotNull(root.get("latitude")));
                predicates.add(cb.isNotNull(root.get("longitude")));
                distanceKm = ListingGeoQuery.distanceKm(cb, root, search.origin());
                if (search.hasRadius()) {
                    ListingGeoQuery.addBoundingBox(predicates, cb, root, search.origin(), search.radiusKm());
                    predicates.add(cb.le(distanceKm, search.radiusKm().doubleValue()));
                }
            }
            if (query != null && !isCountQuery(query)) {
                query.orderBy(orderOf(cb, root, search.sort(), distanceKm));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static List<Order> orderOf(
            CriteriaBuilder cb,
            Root<ListingJpaEntity> root,
            ListingSort sort,
            Expression<Double> distanceKm
    ) {
        List<Order> orders = new ArrayList<>();
        Expression<Integer> promotedFirst = cb.<Integer>selectCase()
                .when(cb.and(
                        cb.isNotNull(root.get("promotedUntil")),
                        cb.greaterThan(root.<Instant>get("promotedUntil"), Instant.now())
                ), Integer.valueOf(0))
                .otherwise(Integer.valueOf(1));
        orders.add(cb.asc(promotedFirst));
        ListingSort safe = sort == null ? ListingSort.NEWEST : sort;
        switch (safe) {
            case PRICE_ASC -> {
                orders.add(cb.asc(root.get("price")));
                orders.add(cb.desc(root.get("createdAt")));
            }
            case PRICE_DESC -> {
                orders.add(cb.desc(root.get("price")));
                orders.add(cb.desc(root.get("createdAt")));
            }
            case OLDEST -> orders.add(cb.asc(root.get("createdAt")));
            case DISTANCE -> {
                if (distanceKm != null) {
                    orders.add(cb.asc(distanceKm));
                }
                orders.add(cb.desc(root.get("createdAt")));
            }
            case NEWEST -> orders.add(cb.desc(root.get("createdAt")));
        }
        return orders;
    }

    private static boolean isCountQuery(jakarta.persistence.criteria.CriteriaQuery<?> query) {
        return query.getResultType() == Long.class || query.getResultType() == long.class;
    }

    private ListingJpaEntity toEntity(Listing listing) {
        ListingJpaEntity entity = new ListingJpaEntity();
        entity.setId(listing.id().value());
        entity.setSellerId(listing.sellerId().value());
        entity.setTitle(listing.title());
        entity.setPrice(listing.price().amount());
        entity.setCategory(listing.category().name());
        entity.setLocation(listing.location().city());
        GeoCoordinates coordinates = listing.location().coordinates();
        entity.setLatitude(coordinates == null ? null : BigDecimal.valueOf(coordinates.latitude()));
        entity.setLongitude(coordinates == null ? null : BigDecimal.valueOf(coordinates.longitude()));
        entity.setImageSrc(listing.imageSrc());
        entity.setStatus(ListingJpaStatus.valueOf(listing.status().name()));
        entity.setViews(listing.views());
        entity.setChats(listing.chats());
        entity.setCreatedAt(listing.createdAt());
        entity.setUpdatedAt(listing.updatedAt());
        entity.setPromotedUntil(listing.promotedUntil());
        return entity;
    }

    private Listing toDomain(ListingJpaEntity entity) {
        return Listing.rehydrate(
                new ListingId(entity.getId()),
                new UserId(entity.getSellerId()),
                entity.getTitle(),
                Money.of(entity.getPrice()),
                new Category(entity.getCategory()),
                new Location(entity.getLocation(), toCoordinates(entity.getLatitude(), entity.getLongitude())),
                entity.getImageSrc(),
                ListingStatus.valueOf(entity.getStatus().name()),
                entity.getViews(),
                entity.getChats(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPromotedUntil()
        );
    }

    private static GeoCoordinates toCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return new GeoCoordinates(latitude.doubleValue(), longitude.doubleValue());
    }
}
