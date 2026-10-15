package com.nimmda.domain.banner;

import java.util.Optional;

public interface HomepageBannerRepository {

    HomepageBanner save(HomepageBanner banner);

    Optional<HomepageBanner> find();
}
