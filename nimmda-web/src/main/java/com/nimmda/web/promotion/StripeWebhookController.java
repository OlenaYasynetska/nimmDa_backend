package com.nimmda.web.promotion;

import com.nimmda.application.promotion.ApplyPromotionWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private final ApplyPromotionWebhookService applyPromotionWebhookService;

    public StripeWebhookController(ApplyPromotionWebhookService applyPromotionWebhookService) {
        this.applyPromotionWebhookService = applyPromotionWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(HttpServletRequest request) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        applyPromotionWebhookService.execute(payload, request.getHeader("Stripe-Signature"));
        return ResponseEntity.ok().build();
    }
}
