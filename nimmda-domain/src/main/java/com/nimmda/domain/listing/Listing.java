package com.nimmda.domain.listing;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Listing {

    private final ListingId id;
    private final UserId sellerId;
    private String title;
    private Money price;
    private Category category;
    private Location location;
    private String imageSrc;
    private List<String> images;
    private String description;
    private ListingStatus status;
    private int views;
    private int chats;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant promotedUntil;
    private Instant promotedAt;

    private Listing(
            ListingId id,
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            List<String> images,
            String description,
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt,
            Instant promotedUntil,
            Instant promotedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        List<String> normalizedImages = normalizeImages(images, imageSrc);
        this.images = normalizedImages;
        this.imageSrc = normalizedImages.get(0);
        this.description = normalizeDescription(description);
        this.status = Objects.requireNonNull(status);
        this.views = requireNonNegative(views, "views");
        this.chats = requireNonNegative(chats, "chats");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.promotedUntil = promotedUntil;
        this.promotedAt = promotedAt;
    }

    public static Listing publishNew(
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            List<String> images,
            String description
    ) {
        Instant now = Instant.now();
        return new Listing(
                ListingId.newId(),
                sellerId,
                title,
                price,
                category,
                location,
                imageSrc,
                images,
                description,
                ListingStatus.ACTIVE,
                0,
                0,
                now,
                now,
                null,
                null
        );
    }

    public static Listing rehydrate(
            ListingId id,
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            List<String> images,
            String description,
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt,
            Instant promotedUntil,
            Instant promotedAt
    ) {
        return new Listing(
                id,
                sellerId,
                title,
                price,
                category,
                location,
                imageSrc,
                images,
                description,
                status,
                views,
                chats,
                createdAt,
                updatedAt,
                promotedUntil,
                promotedAt
        );
    }

    public void markPromotedUntil(Instant until) {
        Objects.requireNonNull(until, "promotedUntil must not be null");
        Instant now = Instant.now();
        if (!until.isAfter(now)) {
            throw new IllegalArgumentException("promotedUntil must be in the future");
        }
        this.promotedUntil = until;
        this.promotedAt = now;
        this.updatedAt = now;
    }

    public boolean isPromoted(Instant now) {
        return promotedUntil != null && promotedUntil.isAfter(now);
    }

    public void pause() {
        this.status = ListingStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = ListingStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            List<String> images,
            String description
    ) {
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        if (images != null || (imageSrc != null && !imageSrc.isBlank())) {
            List<String> normalizedImages = normalizeImages(images, imageSrc == null ? this.imageSrc : imageSrc);
            this.images = normalizedImages;
            this.imageSrc = normalizedImages.get(0);
        }
        this.description = normalizeDescription(description);
        this.updatedAt = Instant.now();
    }

    public void recordView() {
        this.views += 1;
        this.updatedAt = Instant.now();
    }

    public void recordChat() {
        this.chats += 1;
        this.updatedAt = Instant.now();
    }

    public boolean isPublished() {
        return status == ListingStatus.ACTIVE;
    }

    public ListingId id() {
        return id;
    }

    public UserId sellerId() {
        return sellerId;
    }

    public String title() {
        return title;
    }

    public Money price() {
        return price;
    }

    public Category category() {
        return category;
    }

    public Location location() {
        return location;
    }

    public String imageSrc() {
        return imageSrc;
    }

    public List<String> images() {
        return images;
    }

    public String description() {
        return description;
    }

    public ListingStatus status() {
        return status;
    }

    public int views() {
        return views;
    }

    public int chats() {
        return chats;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant promotedUntil() {
        return promotedUntil;
    }

    public Instant promotedAt() {
        return promotedAt;
    }

    private static String requireTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        String normalized = title.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }
        String normalized = description.trim();
        if (normalized.length() > 4000) {
            throw new IllegalArgumentException("description must be at most 4000 characters");
        }
        return normalized;
    }

    private static List<String> normalizeImages(List<String> images, String imageSrc) {
        List<String> source = images == null ? List.of() : images;
        if (source.size() > 5) {
            throw new IllegalArgumentException("at most 5 photos");
        }
        List<String> cleaned = new ArrayList<>();
        for (String image : source) {
            if (image == null || image.isBlank()) {
                continue;
            }
            String value = image.trim();
            if (value.length() > 1_500_000) {
                throw new IllegalArgumentException("photo is too large");
            }
            cleaned.add(value);
        }
        if (cleaned.isEmpty()) {
            cleaned.add(requireImage(imageSrc));
        }
        return List.copyOf(cleaned);
    }

    private static String requireImage(String imageSrc) {
        Objects.requireNonNull(imageSrc, "imageSrc must not be null");
        String normalized = imageSrc.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("imageSrc must not be blank");
        }
        return normalized;
    }

    private static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }
}
