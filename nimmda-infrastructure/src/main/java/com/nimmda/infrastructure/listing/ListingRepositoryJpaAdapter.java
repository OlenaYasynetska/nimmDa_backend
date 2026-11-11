package com.nimmda.infrastructure.listing;

import com.nimmda.application.listing.ListingPhotoAddresses;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper IMAGES = new ObjectMapper();
    private static final TypeReference<List<String>> IMAGE_LIST = new TypeReference<>() {
    };

    private final SpringDataListingJpaRepository jpaRepository;
    private final EntityManager entityManager;
    private final ListingPhotoAddresses photoAddresses;

    public ListingRepositoryJpaAdapter(
            SpringDataListingJpaRepository jpaRepository,
            EntityManager entityManager,
            ListingPhotoAddresses photoAddresses
    ) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
        this.photoAddresses = photoAddresses;
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ListingCard> rows = cb.createQuery(ListingCard.class);
        Root<ListingJpaEntity> root = rows.from(ListingJpaEntity.class);
        rows.select(cardSelection(cb, root));
        Specification<ListingJpaEntity> spec = publishedSpec(criteria);
        rows.where(spec.toPredicate(root, rows, cb));
        TypedQuery<ListingCard> query = entityManager.createQuery(rows);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);

        CriteriaQuery<Long> count = cb.createQuery(Long.class);
        Root<ListingJpaEntity> countRoot = count.from(ListingJpaEntity.class);
        count.select(cb.count(countRoot));
        count.where(spec.toPredicate(countRoot, count, cb));
        long total = entityManager.createQuery(count).getSingleResult();
        return new ListingSearchResult(
                query.getResultList().stream().map(this::cardToDomain).toList(),
                safePage,
                safeSize,
                total
        );
    }

    @Override
    public List<Listing> findBySellerId(UserId sellerId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ListingCard> rows = cb.createQuery(ListingCard.class);
        Root<ListingJpaEntity> root = rows.from(ListingJpaEntity.class);
        rows.select(cardSelection(cb, root));
        rows.where(cb.equal(root.get("sellerId"), sellerId.value()));
        rows.orderBy(cb.desc(root.get("createdAt")));
        return entityManager.createQuery(rows).getResultList().stream().map(this::cardToDomain).toList();
    }

    @Override
    public List<Listing> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ListingCard> rows = cb.createQuery(ListingCard.class);
        Root<ListingJpaEntity> root = rows.from(ListingJpaEntity.class);
        rows.select(cardSelection(cb, root));
        return entityManager.createQuery(rows).getResultList().stream().map(this::cardToDomain).toList();
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
        ListingSort safe = sort == null ? ListingSort.NEWEST : sort;
        if (safe != ListingSort.POSTED) {
            Expression<Integer> promotedFirst = cb.<Integer>selectCase()
                    .when(cb.and(
                            cb.isNotNull(root.get("promotedUntil")),
                            cb.greaterThan(root.<Instant>get("promotedUntil"), Instant.now())
                    ), Integer.valueOf(0))
                    .otherwise(Integer.valueOf(1));
            orders.add(cb.asc(promotedFirst));
            orders.add(cb.desc(root.get("promotedUntil")));
        }
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
            case NEWEST, POSTED -> orders.add(cb.desc(root.get("createdAt")));
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
        entity.setImages(writeImages(listing.images()));
        entity.setDescription(listing.description().isBlank() ? null : listing.description());
        entity.setStatus(ListingJpaStatus.valueOf(listing.status().name()));
        entity.setViews(listing.views());
        entity.setChats(listing.chats());
        entity.setCreatedAt(listing.createdAt());
        entity.setUpdatedAt(listing.updatedAt());
        entity.setPromotedUntil(listing.promotedUntil());
        entity.setPhotoCount(Math.max(1, Math.min(listing.images().size(), 5)));
        return entity;
    }

    private static String writeImages(List<String> images) {
        try {
            return IMAGES.writeValueAsString(images);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("photos could not be stored", exception);
        }
    }

    private static List<String> readImages(String raw, String imageSrc) {
        if (raw == null || raw.isBlank()) {
            return imageSrc == null || imageSrc.isBlank() ? List.of() : List.of(imageSrc);
        }
        try {
            List<String> parsed = IMAGES.readValue(raw, IMAGE_LIST);
            if (parsed == null || parsed.isEmpty()) {
                return imageSrc == null || imageSrc.isBlank() ? List.of() : List.of(imageSrc);
            }
            return parsed;
        } catch (JsonProcessingException exception) {
            return imageSrc == null || imageSrc.isBlank() ? List.of() : List.of(imageSrc);
        }
    }

    private CompoundSelection<ListingCard> cardSelection(CriteriaBuilder cb, Root<ListingJpaEntity> root) {
        return cb.construct(
                ListingCard.class,
                root.get("id"),
                root.get("sellerId"),
                root.get("title"),
                root.get("price"),
                root.get("category"),
                root.get("location"),
                root.get("latitude"),
                root.get("longitude"),
                root.get("description"),
                root.get("status"),
                root.get("views"),
                root.get("chats"),
                root.get("createdAt"),
                root.get("updatedAt"),
                root.get("promotedUntil"),
                root.get("photoCount")
        );
    }

    private Listing cardToDomain(ListingCard card) {
        List<String> photos = photoAddresses.links(card.id(), card.photoCount(), card.updatedAt());
        return Listing.rehydrate(
                new ListingId(card.id()),
                new UserId(card.sellerId()),
                card.title(),
                Money.of(card.price()),
                new Category(card.category()),
                new Location(card.location(), toCoordinates(card.latitude(), card.longitude())),
                photos.get(0),
                photos,
                card.description(),
                ListingStatus.valueOf(card.status().name()),
                card.views(),
                card.chats(),
                card.createdAt(),
                card.updatedAt(),
                card.promotedUntil()
        );
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
                readImages(entity.getImages(), entity.getImageSrc()),
                entity.getDescription(),
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
