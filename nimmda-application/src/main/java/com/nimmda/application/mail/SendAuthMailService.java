package com.nimmda.application.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class SendAuthMailService implements SendAuthMailUseCase {

    private static final Set<String> TYPES = Set.of("verify", "reset");

    private final MailSender mailSender;

    public SendAuthMailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean execute(SendAuthMailCommand command) {
        String type = requireType(command.type());
        boolean reset = "reset".equals(type);
        String subject = reset ? "NimmDa: Passwort zurücksetzen" : "NimmDa: E-Mail bestätigen";
        String action = reset ? "Passwort zurücksetzen" : "E-Mail bestätigen";
        String intro = reset
                ? "Du hast eine Zurücksetzung deines Passworts angefordert."
                : "Willkommen bei NimmDa. Bitte bestätige deine E-Mail, um dein Verkäuferkonto zu aktivieren.";
        String html = """
                <div style="font-family:sans-serif;line-height:1.5;color:#1b3a5f">
                  <h2>NimmDa</h2>
                  <p>%s</p>
                  <p><a href="%s" style="display:inline-block;background:#2f9e57;color:#fff;padding:10px 16px;border-radius:8px;text-decoration:none">%s</a></p>
                  <p style="color:#64748b;font-size:13px">Oder öffne diesen Link:<br>%s</p>
                </div>
                """.formatted(intro, command.link(), action, command.link());
        return mailSender.send(new MailMessage(command.to(), subject, html));
    }

    private static String requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Unknown mail type");
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if (!TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Unknown mail type");
        }
        return normalized;
    }
}
