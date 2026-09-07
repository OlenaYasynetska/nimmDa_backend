package com.nimmda.infrastructure.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendMailSenderAdapter {

    private static final Logger log = LoggerFactory.getLogger(ResendMailSenderAdapter.class);

    private final Resend client;
    private final String from;
    private final boolean configured;

    public ResendMailSenderAdapter(
            @Value("${app.mail.resend.api-key:}") String apiKey,
            @Value("${app.mail.from:NimmDa <noreply@nimmda.org>}") String from
    ) {
        String key = apiKey == null ? "" : apiKey.trim();
        this.configured = !key.isBlank();
        this.client = this.configured ? new Resend(key) : null;
        this.from = from == null || from.isBlank() ? "NimmDa <noreply@nimmda.org>" : from.trim();
    }

    @PostConstruct
    void logReady() {
        if (configured) {
            log.info("Resend transactional mail is configured (from {})", from);
        } else {
            log.warn("Resend is not configured. Set RESEND_API_KEY on nimmDa_backend.");
        }
    }

    public boolean configured() {
        return configured;
    }

    public boolean send(MailMessage message) {
        if (!configured || client == null) {
            return false;
        }
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(message.to())
                .subject(message.subject())
                .html(message.htmlBody())
                .build();
        try {
            CreateEmailResponse response = client.emails().send(params);
            log.info("Resend sent mail to {} id={}", message.to(), response.getId());
            return true;
        } catch (ResendException ex) {
            log.error("Resend failed for {}: {}", message.to(), ex.getMessage(), ex);
            return false;
        }
    }
}
