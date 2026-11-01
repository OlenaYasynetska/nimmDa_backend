package com.nimmda.domain.listing;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;

public final class Listing {

    private final ListingId id;
    private final UserId sellerId;
    private String title;
    private Money price;
    private Category category;
    private Location location;
    private String imageSrc;
    private String description;
    private ListingStatus status;
    private int views;
    private int chats;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant promotedUntil;

    private Listing(
            ListingId id,
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            String description,
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt,
            Instant promotedUntil
    ) {
        this.id = Objects.requireNonNull(id);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        this.imageSrc = requireImage(imageSrc);
        this.description = normalizeDescription(description);
        this.status = Objects.requireNonNull(status);
        this.views = requireNonNegative(views, "views");
        this.chats = requireNonNegative(chats, "chats");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.promotedUntil = promotedUntil;
    }

    public static Listing publishNew(
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
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
                description,
                ListingStatus.ACTIVE,
                0,
                0,
                now,
                now,
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
            String description,
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt,
            Instant promotedUntil
    ) {
        return new Listing(
                id,
                sellerId,
                title,
                price,
                category,
                location,
                imageSrc,
                description,
                status,
                views,
                chats,
                createdAt,
                updatedAt,
                promotedUntil
        );
    }

    public void markPromotedUntil(Instant until) {
        Objects.requireNonNull(until, "promotedUntil must not be null");
        if (!until.isAfter(Instant.now())) {
            throw new IllegalArgumentException("promotedUntil must be in the future");
        }
        this.promotedUntil = until;
        this.updatedAt = Instant.now();
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
            String description
    ) {
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        if (imageSrc != null && !imageSrc.isBlank()) {
            this.imageSrc = requireImage(imageSrc);
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
