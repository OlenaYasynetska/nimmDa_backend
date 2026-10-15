package com.nimmda.application.promotion;

import java.time.Instant;

public record PromotionStatusView(String status, Instant promotedUntil) {
}
