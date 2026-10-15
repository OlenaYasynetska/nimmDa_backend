package com.nimmda.web.promotion;

import com.nimmda.application.promotion.ConfirmListingPromotionService;
import com.nimmda.application.promotion.PromotionCheckoutView;
import com.nimmda.application.promotion.PromotionStatusView;
import com.nimmda.application.promotion.StartListingPromotionService;
import com.nimmda.web.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromotionController {

    private final StartListingPromotionService startListingPromotionService;
    private final ConfirmListingPromotionService confirmListingPromotionService;

    public PromotionController(
            StartListingPromotionService startListingPromotionService,
            ConfirmListingPromotionService confirmListingPromotionService
    ) {
        this.startListingPromotionService = startListingPromotionService;
        this.confirmListingPromotionService = confirmListingPromotionService;
    }

    @PostMapping("/listings/{id}/promotion-checkout")
    public PromotionCheckoutView checkout(Authentication authentication, @PathVariable String id) {
        return startListingPromotionService.execute(id, AuthenticatedUser.id(authentication));
    }

    @PostMapping("/promotions/checkout-session")
    public PromotionStatusView confirm(
            Authentication authentication,
            @Valid @RequestBody ConfirmPromotionRequest request
    ) {
        return confirmListingPromotionService.execute(
                AuthenticatedUser.id(authentication),
                request.sessionId()
        );
    }

    public record ConfirmPromotionRequest(@NotBlank String sessionId) {
    }
}
