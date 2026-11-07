package com.nimmda.application.business;

import java.time.Instant;
import java.util.List;

public record BusinessProfileView(
        String userId,
        String companyName,
        String trade,
        String location,
        String street,
        String postalCode,
        String phone,
        boolean active,
        Instant activeUntil,
        List<BusinessListingView> listings
) {
}
