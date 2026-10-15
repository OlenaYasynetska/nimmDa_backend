package com.nimmda.infrastructure.promotion;

import com.nimmda.application.promotion.CheckoutRequest;
import com.nimmda.application.promotion.PaidCheckout;
import com.nimmda.application.promotion.PromotionCheckout;
import com.nimmda.application.promotion.PromotionException;
import com.nimmda.application.promotion.PromotionWebhook;
import com.nimmda.application.promotion.StartedCheckout;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StripePromotionAdapter implements PromotionCheckout, PromotionWebhook {

    private static final Logger log = LoggerFactory.getLogger(StripePromotionAdapter.class);

    private final String secretKey;
    private final String webhookSecret;

    public StripePromotionAdapter(
            @Value("${app.stripe.secret-key:}") String secretKey,
            @Value("${app.stripe.webhook-secret:}") String webhookSecret
    ) {
        this.secretKey = secretKey == null ? "" : secretKey.trim();
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    }

    @Override
    public boolean configured() {
        return !secretKey.isBlank();
    }

    @Override
    public StartedCheckout start(CheckoutRequest request) {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setAutomaticTax(SessionCreateParams.AutomaticTax.builder().setEnabled(false).build())
                .setSuccessUrl(request.successUrl())
                .setCancelUrl(request.cancelUrl())
                .setCustomerEmail(request.sellerEmail())
                .setLocale(SessionCreateParams.Locale.DE)
                .putMetadata("orderId", request.orderId())
                .putMetadata("kind", request.kind() == null || request.kind().isBlank() ? "listing" : request.kind())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(request.currency())
                                .setUnitAmount((long) request.amountCents())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(request.productName())
                                        .setDescription(request.description())
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            Session session = Session.create(params, requestOptions(request.orderId()));
            if (session.getUrl() == null || session.getUrl().isBlank()) {
                throw new PromotionException("unavailable", "Die Zahlung konnte nicht gestartet werden.");
            }
            return new StartedCheckout(session.getUrl(), session.getId());
        } catch (PromotionException ex) {
            throw ex;
        } catch (StripeException ex) {
            log.warn("Stripe checkout failed for order {}: {}", request.orderId(), ex.getMessage());
            throw new PromotionException("unavailable", "Die Zahlung konnte nicht gestartet werden.");
        }
    }

    @Override
    public PaidCheckout retrieve(String providerSessionId) {
        try {
            Session session = Session.retrieve(providerSessionId, requestOptions(null));
            String orderId = metadata(session, "orderId");
            return new PaidCheckout(orderId, session.getId(), "paid".equals(session.getPaymentStatus()), kind(session));
        } catch (StripeException ex) {
            log.warn("Stripe session lookup failed: {}", ex.getMessage());
            throw new PromotionException("unavailable", "Der Zahlungsstatus konnte nicht geprüft werden.");
        }
    }

    @Override
    public Optional<PaidCheckout> parse(String payload, String signatureHeader) {
        if (webhookSecret.isBlank()) {
            throw new PromotionException("unavailable", "Stripe-Webhook ist nicht konfiguriert.");
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new PromotionException("invalid", "Stripe-Signatur fehlt.");
        }
        final Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new PromotionException("invalid", "Stripe-Signatur ist ungültig.");
        }
        String type = event.getType();
        if (!"checkout.session.completed".equals(type)
                && !"checkout.session.async_payment_succeeded".equals(type)) {
            return Optional.empty();
        }
        StripeObject object = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(object instanceof Session session)) {
            throw new PromotionException("invalid", "Stripe-Ereignis konnte nicht gelesen werden.");
        }
        if (!"paid".equals(session.getPaymentStatus())) {
            return Optional.empty();
        }
        String orderId = metadata(session, "orderId");
        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new PaidCheckout(orderId, session.getId(), true, kind(session)));
    }

    private static String metadata(Session session, String key) {
        if (session.getMetadata() == null) {
            return null;
        }
        return session.getMetadata().get(key);
    }

    private static String kind(Session session) {
        String value = metadata(session, "kind");
        return value == null || value.isBlank() ? "listing" : value;
    }

    private RequestOptions requestOptions(String idempotencyKey) {
        RequestOptions.RequestOptionsBuilder builder = RequestOptions.builder().setApiKey(secretKey);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            builder.setIdempotencyKey(idempotencyKey);
        }
        return builder.build();
    }
}
