package com.nimmda.application.admin;

import java.time.Instant;

public record AdminUserView(
        String id,
        String email,
        String firstName,
        String lastName,
        String accountMode,
        long listingCount,
        Instant createdAt
) {
}
