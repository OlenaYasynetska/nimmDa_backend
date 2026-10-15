package com.nimmda.application.business;

import com.nimmda.domain.business.BusinessProfile;
import com.nimmda.domain.business.BusinessProfileRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveBusinessProfileService {

    private final BusinessProfileRepository profileRepository;
    private final BusinessProfileQuery profileQuery;

    public SaveBusinessProfileService(BusinessProfileRepository profileRepository, BusinessProfileQuery profileQuery) {
        this.profileRepository = profileRepository;
        this.profileQuery = profileQuery;
    }

    @Transactional
    public BusinessProfileView execute(
            String userId,
            String companyName,
            String trade,
            String location,
            String phone
    ) {
        UserId ownerId = new UserId(userId);
        BusinessProfile profile = profileRepository.findByUserId(ownerId)
                .map(current -> {
                    current.updateDetails(companyName, trade, location, phone);
                    return current;
                })
                .orElseGet(() -> BusinessProfile.create(ownerId, companyName, trade, location, phone));
        profileRepository.save(profile);
        return profileQuery.byUser(ownerId);
    }
}
