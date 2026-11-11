package com.nimmda.infrastructure.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class ListingCard {

    private final String id;
    private final String sellerId;
    private final String title;
    private final BigDecimal price;
    private final String category;
    private final String location;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String description;
    private final ListingJpaStatus status;
    private final int views;
    private final int chats;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant promotedUntil;
    private final int photoCount;

    public ListingCard(
            String id,
            String sellerId,
            String title,
            BigDecimal price,
            String category,
            String location,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            ListingJpaStatus status,
            int views,
            int chats,
            Instant createdAt,
            Instant updatedAt,
            Instant promotedUntil,
            int photoCount
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.price = price;
        this.category = category;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.status = status;
        this.views = views;
        this.chats = chats;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.promotedUntil = promotedUntil;
        this.photoCount = photoCount;
    }

    public String id() {
        return id;
    }

    public String sellerId() {
        return sellerId;
    }

    public String title() {
        return title;
    }

    public BigDecimal price() {
        return price;
    }

    public String category() {
        return category;
    }

    public String location() {
        return location;
    }

    public BigDecimal latitude() {
        return latitude;
    }

    public BigDecimal longitude() {
        return longitude;
    }

    public String description() {
        return description;
    }

    public ListingJpaStatus status() {
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

    public int photoCount() {
        return photoCount;
    }
}
