package com.nimmda.application.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class SendPaymentReceiptService {

    private static final Logger log = Logger.getLogger(SendPaymentReceiptService.class.getName());
    private static final ZoneId VIENNA = ZoneId.of("Europe/Vienna");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);

    private final MailSender mailSender;
    private final String sellerName;
    private final String sellerAddress;
    private final String sellerEmail;

    public SendPaymentReceiptService(
            MailSender mailSender,
            @Value("${app.invoice.seller-name:Olena Yasynetska}") String sellerName,
            @Value("${app.invoice.seller-address:Arbeiterstraße 26/3, 4400 Steyr, Österreich}") String sellerAddress,
            @Value("${app.invoice.seller-email:contact@ugs-info.at}") String sellerEmail
    ) {
        this.mailSender = mailSender;
        this.sellerName = sellerName;
        this.sellerAddress = sellerAddress;
        this.sellerEmail = sellerEmail;
    }

    public void send(
            String to,
            String productName,
            String detail,
            int amountCents,
            int days,
            Instant paidAt,
            Instant validUntil,
            String receiptNumber
    ) {
        if (to == null || to.isBlank()) {
            log.warning("Payment receipt skipped, no recipient for " + receiptNumber);
            return;
        }
        if (!mailSender.configured()) {
            log.warning("Payment receipt not sent for " + receiptNumber + ": mail is not configured");
            return;
        }
        String subject = "NimmDa: Zahlungsbeleg " + productName;
        String html = """
                <div style="font-family:sans-serif;line-height:1.5;color:#1b3a5f">
                  <h2>Zahlungsbeleg</h2>
                  <p>%s<br>%s<br>%s</p>
                  <p><strong>Belegnummer:</strong> %s<br>
                  <strong>Datum:</strong> %s<br>
                  <strong>Leistung:</strong> %s, %d Tage<br>
                  <strong>Details:</strong> %s<br>
                  <strong>Betrag:</strong> %s<br>
                  <strong>Gültig bis:</strong> %s</p>
                  <p>Kleinunternehmerin gemäß § 6 Abs. 1 Z 27 UStG. Es wird keine Umsatzsteuer ausgewiesen. Der genannte Betrag ist der Endbetrag.</p>
                  <p style="color:#64748b;font-size:13px">Die Leistung beginnt erst nach erfolgreicher Zahlung und endet automatisch mit dem genannten Datum.</p>
                </div>
                """.formatted(
                escape(sellerName),
                escape(sellerAddress),
                escape(sellerEmail),
                escape(receiptNumber),
                format(paidAt),
                escape(productName),
                days,
                escape(detail),
                formatEuro(amountCents),
                format(validUntil)
        );
        try {
            boolean sent = mailSender.send(new MailMessage(to.trim(), subject, html));
            if (!sent) {
                log.warning("Payment receipt was not delivered to " + to + " for " + receiptNumber);
            }
        } catch (RuntimeException ex) {
            log.severe("Payment receipt failed for " + receiptNumber + ": " + ex.getMessage());
        }
    }

    private static String format(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DATE.format(instant.atZone(VIENNA));
    }

    private static String formatEuro(int cents) {
        int amount = Math.max(cents, 0);
        return String.format(Locale.GERMAN, "%d,%02d €", amount / 100, amount % 100);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
