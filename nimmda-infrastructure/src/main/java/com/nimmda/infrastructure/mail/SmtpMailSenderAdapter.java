package com.nimmda.infrastructure.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpMailSenderAdapter implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final String host;
    private final String username;
    private final String password;
    private final boolean configured;

    public SmtpMailSenderAdapter(
            JavaMailSender mailSender,
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password
    ) {
        this.mailSender = mailSender;
        this.host = host == null || host.isBlank() ? "smtp.gmail.com" : host.trim();
        this.username = username == null ? "" : username.trim();
        this.password = MailSenderConfig.normalizePassword(password);
        this.configured = !this.username.isBlank() && this.password != null && !this.password.isBlank();
    }

    @PostConstruct
    void logReady() {
        if (configured) {
            log.info("SMTP confirmation mail is configured for {}", username);
        } else {
            log.warn("SMTP confirmation mail is NOT configured. Set MAIL_USERNAME and MAIL_PASSWORD on the backend service.");
        }
    }

    @Override
    public boolean configured() {
        return configured;
    }

    @Override
    public boolean send(MailMessage message) {
        if (!configured) {
            log.warn("Mail not sent to {} (MAIL_USERNAME / MAIL_PASSWORD missing)", message.to());
            return false;
        }
        if (deliver(mailSender, message)) {
            return true;
        }
        log.warn("Retrying confirmation mail via SMTPS 465");
        JavaMailSenderImpl fallback = MailSenderConfig.create(host, 465, username, password, true);
        return deliver(fallback, message);
    }

    private boolean deliver(JavaMailSender sender, MailMessage message) {
        try {
            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(username, "NimmDa", StandardCharsets.UTF_8.name()));
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            sender.send(mimeMessage);
            log.info("Mail sent to {}", message.to());
            return true;
        } catch (Exception ex) {
            log.error("Mail failed for {}: {}", message.to(), ex.getMessage(), ex);
            return false;
        }
    }
}
