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
    private ListingStatus status;
    private int views;
    private int chats;
    private final Instant createdAt;
    private Instant updatedAt;

    private Listing(
            ListingId id,
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc,
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        this.imageSrc = requireImage(imageSrc);
        this.status = Objects.requireNonNull(status);
        this.views = requireNonNegative(views, "views");
        this.chats = requireNonNegative(chats, "chats");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Listing publishNew(
            UserId sellerId,
            String title,
            Money price,
            Category category,
            Location location,
            String imageSrc
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
                ListingStatus.ACTIVE,
                0,
                0,
                now,
                now
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
            ListingStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Listing(
                id,
                sellerId,
                title,
                price,
                category,
                location,
                imageSrc,
                status,
                views,
                chats,
                createdAt,
                updatedAt
        );
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
            String imageSrc
    ) {
        this.title = requireTitle(title);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.location = Objects.requireNonNull(location);
        if (imageSrc != null && !imageSrc.isBlank()) {
            this.imageSrc = requireImage(imageSrc);
        }
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

    private static String requireTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        String normalized = title.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
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
