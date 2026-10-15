package com.nimmda.domain.banner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class HomepageBanner {

    public static final String ID = "home";

    private final String advertiser;
    private final String imageSrc;
    private final String targetUrl;
    private final LocalDate startsOn;
    private final LocalDate endsOn;
    private final Instant updatedAt;

    public HomepageBanner(
            String advertiser,
            String imageSrc,
            String targetUrl,
            LocalDate startsOn,
            LocalDate endsOn,
            Instant updatedAt
    ) {
        this.advertiser = advertiser == null ? "" : advertiser.trim();
        this.imageSrc = blankToNull(imageSrc);
        this.targetUrl = blankToNull(targetUrl);
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (this.advertiser.length() > 160) {
            throw new IllegalArgumentException("advertiser is too long");
        }
        if (this.imageSrc != null && this.imageSrc.length() > 1_500_000) {
            throw new IllegalArgumentException("image is too large");
        }
        if (this.targetUrl != null && !this.targetUrl.matches("https?://\\S+")) {
            throw new IllegalArgumentException("link must start with http:// or https://");
        }
        if (this.startsOn != null && this.endsOn != null && this.endsOn.isBefore(this.startsOn)) {
            throw new IllegalArgumentException("end date is before the start date");
        }
    }

    public boolean isVisible(LocalDate today) {
        return imageSrc != null
                && startsOn != null
                && endsOn != null
                && !today.isBefore(startsOn)
                && !today.isAfter(endsOn);
    }

    public String advertiser() {
        return advertiser;
    }

    public String imageSrc() {
        return imageSrc;
    }

    public String targetUrl() {
        return targetUrl;
    }

    public LocalDate startsOn() {
        return startsOn;
    }

    public LocalDate endsOn() {
        return endsOn;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
