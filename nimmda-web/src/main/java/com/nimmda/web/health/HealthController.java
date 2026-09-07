package com.nimmda.web.health;

import com.nimmda.application.port.mail.MailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final MailSender mailSender;

    public HealthController(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "nimmda-backend",
                "timestamp", Instant.now().toString(),
                "mailConfigured", mailSender.configured()
        );
    }
}
