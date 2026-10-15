package com.nimmda.domain.business;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;

public final class BusinessProfile {

    private final UserId userId;
    private String companyName;
    private String trade;
    private String location;
    private String phone;
    private Instant activeUntil;
    private final Instant createdAt;
    private Instant updatedAt;

    private BusinessProfile(
            UserId userId,
            String companyName,
            String trade,
            String location,
            String phone,
            Instant activeUntil,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.userId = Objects.requireNonNull(userId);
        this.companyName = requireText(companyName, "companyName", 160);
        this.trade = requireText(trade, "trade", 160);
        this.location = requireText(location, "location", 128);
        this.phone = requirePhone(phone);
        this.activeUntil = activeUntil;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static BusinessProfile create(
            UserId userId,
            String companyName,
            String trade,
            String location,
            String phone
    ) {
        Instant now = Instant.now();
        return new BusinessProfile(userId, companyName, trade, location, phone, null, now, now);
    }

    public static BusinessProfile rehydrate(
            UserId userId,
            String companyName,
            String trade,
            String location,
            String phone,
            Instant activeUntil,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new BusinessProfile(userId, companyName, trade, location, phone, activeUntil, createdAt, updatedAt);
    }

    public void updateDetails(String companyName, String trade, String location, String phone) {
        this.companyName = requireText(companyName, "companyName", 160);
        this.trade = requireText(trade, "trade", 160);
        this.location = requireText(location, "location", 128);
        this.phone = requirePhone(phone);
        this.updatedAt = Instant.now();
    }

    public void markActiveUntil(Instant until) {
        Objects.requireNonNull(until, "activeUntil must not be null");
        if (!until.isAfter(Instant.now())) {
            throw new IllegalArgumentException("activeUntil must be in the future");
        }
        this.activeUntil = until;
        this.updatedAt = Instant.now();
    }

    public boolean isActive(Instant now) {
        return activeUntil != null && activeUntil.isAfter(now);
    }

    public UserId userId() {
        return userId;
    }

    public String companyName() {
        return companyName;
    }

    public String trade() {
        return trade;
    }

    public String location() {
        return location;
    }

    public String phone() {
        return phone;
    }

    public Instant activeUntil() {
        return activeUntil;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String requireText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > max) {
            throw new IllegalArgumentException(field + " is too long");
        }
        return normalized;
    }

    private static String requirePhone(String phone) {
        String normalized = requireText(phone, "phone", 40);
        if (!normalized.matches("[0-9+()\\-/\\s]{5,40}")) {
            throw new IllegalArgumentException("phone is invalid");
        }
        return normalized;
    }
}
