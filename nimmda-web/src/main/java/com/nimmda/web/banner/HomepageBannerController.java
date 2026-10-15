package com.nimmda.web.banner;

import com.nimmda.application.banner.HomepageBannerService;
import com.nimmda.application.banner.HomepageBannerView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homepage-banner")
public class HomepageBannerController {

    private final HomepageBannerService homepageBannerService;

    public HomepageBannerController(HomepageBannerService homepageBannerService) {
        this.homepageBannerService = homepageBannerService;
    }

    @GetMapping
    public HomepageBannerView banner() {
        return homepageBannerService.publicBanner();
    }
}
