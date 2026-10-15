package com.nimmda.application.banner;

import java.time.LocalDate;

public record HomepageBannerView(
        boolean active,
        String advertiser,
        String imageSrc,
        String targetUrl,
        LocalDate startsOn,
        LocalDate endsOn
) {
}
