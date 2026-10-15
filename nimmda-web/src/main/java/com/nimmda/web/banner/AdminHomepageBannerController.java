package com.nimmda.web.banner;

import com.nimmda.application.banner.HomepageBannerService;
import com.nimmda.application.banner.HomepageBannerView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/homepage-banner")
public class AdminHomepageBannerController {

    private final HomepageBannerService homepageBannerService;

    public AdminHomepageBannerController(HomepageBannerService homepageBannerService) {
        this.homepageBannerService = homepageBannerService;
    }

    @GetMapping
    public HomepageBannerView banner() {
        return homepageBannerService.adminBanner();
    }

    @PutMapping
    public HomepageBannerView save(@Valid @RequestBody SaveHomepageBannerRequest request) {
        return homepageBannerService.save(
                request.advertiser(),
                request.imageSrc(),
                request.targetUrl(),
                request.startsOn(),
                request.endsOn()
        );
    }

    public record SaveHomepageBannerRequest(
            @Size(max = 160) String advertiser,
            String imageSrc,
            @Size(max = 500) String targetUrl,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startsOn,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endsOn
    ) {
    }
}
