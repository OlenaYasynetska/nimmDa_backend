package com.nimmda.domain.promotion;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ListingPromotion {

    private final String id;
    private final ListingId listingId;
    private final UserId sellerId;
    private final int amountCents;
    private final String currency;
    private final int days;
    private PromotionStatus status;
    private String providerSessionId;
    private Instant promotedUntil;
    private final Instant createdAt;
    private Instant paidAt;

    private ListingPromotion(
            String id,
            ListingId listingId,
            UserId sellerId,
            int amountCents,
            String currency,
            int days,
            PromotionStatus status,
            String providerSessionId,
            Instant promotedUntil,
            Instant createdAt,
            Instant paidAt
    ) {
        this.id = requireText(id, "id");
        this.listingId = Objects.requireNonNull(listingId);
        this.sellerId = Objects.requireNonNull(sellerId);
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
        this.amountCents = amountCents;
        this.currency = requireText(currency, "currency");
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }
        this.days = days;
        this.status = Objects.requireNonNull(status);
        this.providerSessionId = blankToNull(providerSessionId);
        this.promotedUntil = promotedUntil;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.paidAt = paidAt;
    }

    public static ListingPromotion start(ListingId listingId, UserId sellerId, int amountCents, String currency, int days) {
        return new ListingPromotion(
                UUID.randomUUID().toString(),
                listingId,
                sellerId,
                amountCents,
                currency,
                days,
                PromotionStatus.PENDING,
                null,
                null,
                Instant.now(),
                null
        );
    }

    public static ListingPromotion rehydrate(
            String id,
            ListingId listingId,
            UserId sellerId,
            int amountCents,
            String currency,
            int days,
            PromotionStatus status,
            String providerSessionId,
            Instant promotedUntil,
            Instant createdAt,
            Instant paidAt
    ) {
        return new ListingPromotion(
                id,
                listingId,
                sellerId,
                amountCents,
                currency,
                days,
                status,
                providerSessionId,
                promotedUntil,
                createdAt,
                paidAt
        );
    }

    public void attachSession(String sessionId) {
        this.providerSessionId = requireText(sessionId, "providerSessionId");
    }

    public void markPaid(Instant paidAt, Instant promotedUntil) {
        this.status = PromotionStatus.PAID;
        this.paidAt = Objects.requireNonNull(paidAt);
        this.promotedUntil = Objects.requireNonNull(promotedUntil);
    }

    public boolean isPaid() {
        return status == PromotionStatus.PAID;
    }

    public String id() {
        return id;
    }

    public ListingId listingId() {
        return listingId;
    }

    public UserId sellerId() {
        return sellerId;
    }

    public int amountCents() {
        return amountCents;
    }

    public String currency() {
        return currency;
    }

    public int days() {
        return days;
    }

    public PromotionStatus status() {
        return status;
    }

    public String providerSessionId() {
        return providerSessionId;
    }

    public Instant promotedUntil() {
        return promotedUntil;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant paidAt() {
        return paidAt;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
