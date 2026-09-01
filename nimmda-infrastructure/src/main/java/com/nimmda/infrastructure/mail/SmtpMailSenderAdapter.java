package com.nimmda.infrastructure.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpMailSenderAdapter implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String from;

    public SmtpMailSenderAdapter(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:NimmDa <noreply@nimmda.at>}") String from
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    @Override
    public boolean send(MailMessage message) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!enabled || sender == null) {
            log.warn("Mail not sent to {} (mail disabled). Subject: {}", message.to(), message.subject());
            return false;
        }
        try {
            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            sender.send(mimeMessage);
            log.info("Mail sent to {}", message.to());
            return true;
        } catch (Exception ex) {
            log.error("Mail failed for {}: {}", message.to(), ex.getMessage());
            return false;
        }
    }
}
