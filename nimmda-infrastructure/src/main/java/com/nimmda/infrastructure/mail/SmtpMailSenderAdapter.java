package com.nimmda.infrastructure.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpMailSenderAdapter implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String username;

    public SmtpMailSenderAdapter(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean enabledFlag,
            @Value("${app.mail.from:NimmDa <noreply@nimmda.org>}") String from,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password
    ) {
        this.mailSender = mailSender;
        this.username = username == null ? "" : username.trim();
        String secret = MailSenderConfig.normalizePassword(password);
        this.enabled = enabledFlag || (!this.username.isBlank() && secret != null && !secret.isBlank());
        this.from = resolveFrom(from, this.username);
    }

    @PostConstruct
    void logReady() {
        if (enabled) {
            log.info("SMTP confirmation mail is on ({})", username);
        } else {
            log.warn("SMTP confirmation mail is off: set MAIL_USERNAME and MAIL_PASSWORD (Gmail app password)");
        }
    }

    @Override
    public boolean send(MailMessage message) {
        if (!enabled) {
            log.warn("Mail not sent to {} (SMTP not configured)", message.to());
            return false;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            mailSender.send(mimeMessage);
            log.info("Mail sent to {}", message.to());
            return true;
        } catch (Exception ex) {
            log.error("Mail failed for {}: {}", message.to(), ex.getMessage(), ex);
            return false;
        }
    }

    private static String resolveFrom(String from, String username) {
        if (from != null && !from.isBlank() && !from.contains("noreply@nimmda")) {
            return from.trim();
        }
        if (!username.isBlank()) {
            return "NimmDa <" + username + ">";
        }
        return from == null || from.isBlank() ? "NimmDa <noreply@nimmda.org>" : from.trim();
    }
}
