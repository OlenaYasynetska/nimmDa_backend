package com.nimmda.application.banner;

import com.nimmda.domain.banner.HomepageBanner;
import com.nimmda.domain.banner.HomepageBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class HomepageBannerService {

    private static final ZoneId VIENNA = ZoneId.of("Europe/Vienna");

    private final HomepageBannerRepository bannerRepository;

    public HomepageBannerService(HomepageBannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Transactional(readOnly = true)
    public HomepageBannerView publicBanner() {
        return bannerRepository.find()
                .filter(banner -> banner.isVisible(LocalDate.now(VIENNA)))
                .map(banner -> new HomepageBannerView(
                        true,
                        banner.advertiser(),
                        banner.imageSrc(),
                        banner.targetUrl(),
                        null,
                        null
                ))
                .orElseGet(() -> new HomepageBannerView(false, null, null, null, null, null));
    }

    @Transactional(readOnly = true)
    public HomepageBannerView adminBanner() {
        return bannerRepository.find()
                .map(banner -> new HomepageBannerView(
                        banner.isVisible(LocalDate.now(VIENNA)),
                        banner.advertiser(),
                        banner.imageSrc(),
                        banner.targetUrl(),
                        banner.startsOn(),
                        banner.endsOn()
                ))
                .orElseGet(() -> new HomepageBannerView(false, "", null, null, null, null));
    }

    @Transactional
    public HomepageBannerView save(
            String advertiser,
            String imageSrc,
            String targetUrl,
            LocalDate startsOn,
            LocalDate endsOn
    ) {
        boolean hasImage = imageSrc != null && !imageSrc.isBlank();
        if (hasImage && (advertiser == null || advertiser.isBlank() || startsOn == null || endsOn == null)) {
            throw new IllegalArgumentException("advertiser, start date and end date are required");
        }
        HomepageBanner banner = new HomepageBanner(
                advertiser,
                imageSrc,
                targetUrl,
                startsOn,
                endsOn,
                Instant.now()
        );
        HomepageBanner saved = bannerRepository.save(banner);
        return new HomepageBannerView(
                saved.isVisible(LocalDate.now(VIENNA)),
                saved.advertiser(),
                saved.imageSrc(),
                saved.targetUrl(),
                saved.startsOn(),
                saved.endsOn()
        );
    }
}
