package com.nimmda.domain.business;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;

public final class BusinessProfile {

    private final UserId userId;
    private String companyName;
    private String trade;
    private String location;
    private String street;
    private String postalCode;
    private String phone;
    private Instant activeUntil;
    private final Instant createdAt;
    private Instant updatedAt;

    private BusinessProfile(
            UserId userId,
            String companyName,
            String trade,
            String location,
            String street,
            String postalCode,
            String phone,
            Instant activeUntil,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.userId = Objects.requireNonNull(userId);
        this.companyName = requireText(companyName, "companyName", 160);
        this.trade = requireText(trade, "trade", 160);
        this.location = requireText(location, "location", 128);
        this.street = optionalText(street, 160);
        this.postalCode = optionalPostalCode(postalCode);
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
            String street,
            String postalCode,
            String phone
    ) {
        requireText(street, "street", 160);
        requirePostalCode(postalCode);
        Instant now = Instant.now();
        return new BusinessProfile(userId, companyName, trade, location, street, postalCode, phone, null, now, now);
    }

    public static BusinessProfile rehydrate(
            UserId userId,
            String companyName,
            String trade,
            String location,
            String street,
            String postalCode,
            String phone,
            Instant activeUntil,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new BusinessProfile(
                userId, companyName, trade, location, street, postalCode, phone, activeUntil, createdAt, updatedAt
        );
    }

    public void updateDetails(
            String companyName,
            String trade,
            String location,
            String street,
            String postalCode,
            String phone
    ) {
        this.companyName = requireText(companyName, "companyName", 160);
        this.trade = requireText(trade, "trade", 160);
        this.location = requireText(location, "location", 128);
        this.street = requireText(street, "street", 160);
        this.postalCode = requirePostalCode(postalCode);
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

    public String street() {
        return street;
    }

    public String postalCode() {
        return postalCode;
    }

    public String invoiceAddress() {
        if (street.isBlank() || postalCode.isBlank()) {
            return location + "\nÖsterreich";
        }
        return street + "\n" + postalCode + " " + location + "\nÖsterreich";
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

    private static String optionalText(String value, int max) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return requireText(value, "street", max);
    }

    private static String optionalPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return "";
        }
        return requirePostalCode(postalCode);
    }

    private static String requirePostalCode(String postalCode) {
        String normalized = requireText(postalCode, "postalCode", 16);
        if (!normalized.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("postalCode is invalid");
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
