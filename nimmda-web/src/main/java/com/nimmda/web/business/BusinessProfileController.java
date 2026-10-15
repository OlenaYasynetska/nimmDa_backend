package com.nimmda.web.business;

import com.nimmda.application.business.BusinessProfileCard;
import com.nimmda.application.business.BusinessProfileQuery;
import com.nimmda.application.business.BusinessProfileView;
import com.nimmda.application.business.ConfirmBusinessSubscriptionService;
import com.nimmda.application.business.SaveBusinessProfileService;
import com.nimmda.application.business.StartBusinessSubscriptionService;
import com.nimmda.application.promotion.PromotionCheckoutView;
import com.nimmda.application.promotion.PromotionStatusView;
import com.nimmda.domain.shared.UserId;
import com.nimmda.web.promotion.PromotionController;
import com.nimmda.web.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business-profiles")
public class BusinessProfileController {

    private final BusinessProfileQuery businessProfileQuery;
    private final SaveBusinessProfileService saveBusinessProfileService;
    private final StartBusinessSubscriptionService startBusinessSubscriptionService;
    private final ConfirmBusinessSubscriptionService confirmBusinessSubscriptionService;

    public BusinessProfileController(
            BusinessProfileQuery businessProfileQuery,
            SaveBusinessProfileService saveBusinessProfileService,
            StartBusinessSubscriptionService startBusinessSubscriptionService,
            ConfirmBusinessSubscriptionService confirmBusinessSubscriptionService
    ) {
        this.businessProfileQuery = businessProfileQuery;
        this.saveBusinessProfileService = saveBusinessProfileService;
        this.startBusinessSubscriptionService = startBusinessSubscriptionService;
        this.confirmBusinessSubscriptionService = confirmBusinessSubscriptionService;
    }

    @GetMapping
    public List<BusinessProfileCard> active(@RequestParam(required = false) String location) {
        return businessProfileQuery.activeCards(location);
    }

    @GetMapping("/mine")
    public BusinessProfileView mine(Authentication authentication) {
        return businessProfileQuery.byUser(new UserId(AuthenticatedUser.id(authentication)));
    }

    @PutMapping("/mine")
    public BusinessProfileView save(Authentication authentication, @Valid @RequestBody SaveBusinessProfileRequest request) {
        return saveBusinessProfileService.execute(
                AuthenticatedUser.id(authentication),
                request.companyName(),
                request.trade(),
                request.location(),
                request.phone()
        );
    }

    @PostMapping("/checkout")
    public PromotionCheckoutView checkout(Authentication authentication) {
        return startBusinessSubscriptionService.execute(AuthenticatedUser.id(authentication));
    }

    @PostMapping("/checkout-session")
    public PromotionStatusView confirm(
            Authentication authentication,
            @Valid @RequestBody PromotionController.ConfirmPromotionRequest request
    ) {
        return confirmBusinessSubscriptionService.execute(
                AuthenticatedUser.id(authentication),
                request.sessionId()
        );
    }

    @GetMapping("/{userId}")
    public BusinessProfileView profile(@PathVariable String userId) {
        return businessProfileQuery.byUser(new UserId(userId));
    }

    public record SaveBusinessProfileRequest(
            @NotBlank @Size(max = 160) String companyName,
            @NotBlank @Size(max = 160) String trade,
            @NotBlank @Size(max = 128) String location,
            @NotBlank @Size(max = 40) @Pattern(regexp = "[0-9+()\\-/\\s]{5,40}") String phone
    ) {
    }
}
