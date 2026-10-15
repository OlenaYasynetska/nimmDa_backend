package com.nimmda.domain.business;

import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository {

    BusinessProfile save(BusinessProfile profile);

    Optional<BusinessProfile> findByUserId(UserId userId);

    List<BusinessProfile> findActive(Instant now, String location);
}
