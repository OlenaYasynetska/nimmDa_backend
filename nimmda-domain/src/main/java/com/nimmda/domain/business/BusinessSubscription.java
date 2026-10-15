package com.nimmda.domain.business;

import com.nimmda.domain.promotion.PromotionStatus;
import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class BusinessSubscription {

    private final String id;
    private final UserId userId;
    private final int amountCents;
    private final String currency;
    private final int days;
    private PromotionStatus status;
    private String providerSessionId;
    private Instant activeUntil;
    private final Instant createdAt;
    private Instant paidAt;

    private BusinessSubscription(
            String id,
            UserId userId,
            int amountCents,
            String currency,
            int days,
            PromotionStatus status,
            String providerSessionId,
            Instant activeUntil,
            Instant createdAt,
            Instant paidAt
    ) {
        this.id = requireText(id);
        this.userId = Objects.requireNonNull(userId);
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
        this.amountCents = amountCents;
        this.currency = requireText(currency);
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }
        this.days = days;
        this.status = Objects.requireNonNull(status);
        this.providerSessionId = blankToNull(providerSessionId);
        this.activeUntil = activeUntil;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.paidAt = paidAt;
    }

    public static BusinessSubscription start(UserId userId, int amountCents, String currency, int days) {
        return new BusinessSubscription(
                UUID.randomUUID().toString(),
                userId,
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

    public static BusinessSubscription rehydrate(
            String id,
            UserId userId,
            int amountCents,
            String currency,
            int days,
            PromotionStatus status,
            String providerSessionId,
            Instant activeUntil,
            Instant createdAt,
            Instant paidAt
    ) {
        return new BusinessSubscription(
                id,
                userId,
                amountCents,
                currency,
                days,
                status,
                providerSessionId,
                activeUntil,
                createdAt,
                paidAt
        );
    }

    public void attachSession(String sessionId) {
        this.providerSessionId = requireText(sessionId);
    }

    public void markPaid(Instant paidAt, Instant activeUntil) {
        this.status = PromotionStatus.PAID;
        this.paidAt = Objects.requireNonNull(paidAt);
        this.activeUntil = Objects.requireNonNull(activeUntil);
    }

    public boolean isPaid() {
        return status == PromotionStatus.PAID;
    }

    public String id() {
        return id;
    }

    public UserId userId() {
        return userId;
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

    public Instant activeUntil() {
        return activeUntil;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant paidAt() {
        return paidAt;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
