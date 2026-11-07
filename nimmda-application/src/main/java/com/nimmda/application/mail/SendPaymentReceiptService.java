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
    private final String copyTo;

    public SendPaymentReceiptService(
            MailSender mailSender,
            @Value("${app.invoice.seller-name:Olena Yasynetska}") String sellerName,
            @Value("${app.invoice.seller-address:Arbeiterstraße 26/3, 4400 Steyr, Österreich}") String sellerAddress,
            @Value("${app.invoice.seller-email:contact@ugs-info.at}") String sellerEmail,
            @Value("${app.invoice.copy-to:olenayasynetska8@gmail.com}") String copyTo
    ) {
        this.mailSender = mailSender;
        this.sellerName = sellerName;
        this.sellerAddress = sellerAddress;
        this.sellerEmail = sellerEmail;
        this.copyTo = copyTo == null ? "" : copyTo.trim();
    }

    public void send(
            String to,
            String buyerName,
            String buyerAddress,
            String productName,
            String detail,
            int amountCents,
            int days,
            Instant paidAt,
            Instant validUntil,
            String receiptNumber
    ) {
        String customer = to == null ? "" : to.trim();
        if (customer.isBlank() && copyTo.isBlank()) {
            log.warning("Payment receipt skipped, no recipient for " + receiptNumber);
            return;
        }
        if (!mailSender.configured()) {
            log.warning("Payment receipt not sent for " + receiptNumber + ": mail is not configured");
            return;
        }
        String number = receiptNumber == null ? "" : receiptNumber.trim();
        String subject = "Rechnung " + number + " – NimmDa";
        String html = """
                <div style="font-family:Arial,sans-serif;line-height:1.45;color:#1b3a5f;max-width:640px">
                  <h1 style="font-size:22px;margin:0 0 16px">Rechnung</h1>
                  <p style="margin:0 0 16px">%s<br>%s<br>%s</p>
                  <p style="margin:0 0 16px"><strong>Rechnungsempfänger</strong><br>%s</p>
                  <p style="margin:0 0 16px">
                    <strong>Rechnungsnummer:</strong> %s<br>
                    <strong>Rechnungsdatum:</strong> %s<br>
                    <strong>Leistungszeitraum:</strong> %s bis %s
                  </p>
                  <table style="width:100%%;border-collapse:collapse;margin:0 0 16px">
                    <thead>
                      <tr>
                        <th style="text-align:left;border-bottom:1px solid #cbd5e1;padding:6px 4px">Pos.</th>
                        <th style="text-align:left;border-bottom:1px solid #cbd5e1;padding:6px 4px">Beschreibung</th>
                        <th style="text-align:right;border-bottom:1px solid #cbd5e1;padding:6px 4px">Menge</th>
                        <th style="text-align:right;border-bottom:1px solid #cbd5e1;padding:6px 4px">Einzelpreis</th>
                        <th style="text-align:right;border-bottom:1px solid #cbd5e1;padding:6px 4px">Betrag</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td style="padding:8px 4px;vertical-align:top">1</td>
                        <td style="padding:8px 4px">%s</td>
                        <td style="padding:8px 4px;text-align:right;vertical-align:top">1</td>
                        <td style="padding:8px 4px;text-align:right;vertical-align:top">%s</td>
                        <td style="padding:8px 4px;text-align:right;vertical-align:top">%s</td>
                      </tr>
                    </tbody>
                  </table>
                  <p style="margin:0 0 16px"><strong>Gesamtbetrag: %s</strong><br>Der Betrag wurde am %s vollständig bezahlt.</p>
                  <p style="margin:0 0 16px"><strong>Keine Umsatzsteuer.</strong> Die Rechnungsstellerin ist Kleinunternehmerin gemäß § 6 Abs. 1 Z 27 UStG und führt keine Umsatzsteuer ab. Der Gesamtbetrag ist der Endbetrag.</p>
                  <p style="color:#64748b;font-size:13px;margin:0">NimmDa · nimmda.org</p>
                </div>
                """.formatted(
                escape(sellerName),
                escape(sellerAddress),
                escape(sellerEmail),
                recipientBlock(buyerName, buyerAddress, customer.isBlank() ? copyTo : customer),
                escape(number),
                format(paidAt),
                format(paidAt),
                format(validUntil),
                escape(description(productName, detail, days)),
                formatEuro(amountCents),
                formatEuro(amountCents),
                formatEuro(amountCents),
                format(paidAt)
        );
        if (!customer.isBlank()) {
            deliver(customer, subject, html, receiptNumber);
        }
        if (!copyTo.isBlank() && !copyTo.equalsIgnoreCase(customer)) {
            deliver(copyTo, "Kopie: " + subject, html, receiptNumber);
        }
    }

    private void deliver(String recipient, String subject, String html, String receiptNumber) {
        try {
            boolean sent = mailSender.send(new MailMessage(recipient, subject, html));
            if (!sent) {
                log.warning("Payment receipt was not delivered to " + recipient + " for " + receiptNumber);
            }
        } catch (RuntimeException ex) {
            log.severe("Payment receipt failed for " + receiptNumber + " to " + recipient + ": " + ex.getMessage());
        }
    }

    private static String recipientBlock(String buyerName, String buyerAddress, String email) {
        StringBuilder block = new StringBuilder(escape(buyerLine(buyerName)));
        if (buyerAddress != null) {
            for (String line : buyerAddress.split("\\R")) {
                if (!line.isBlank()) {
                    block.append("<br>").append(escape(line.trim()));
                }
            }
        }
        block.append("<br>").append(escape(email));
        return block.toString();
    }

    private static String buyerLine(String buyerName) {
        if (buyerName == null || buyerName.isBlank()) {
            return "Kontoinhaber";
        }
        return buyerName.trim();
    }

    private static String description(String productName, String detail, int days) {
        String product = productName == null || productName.isBlank() ? "Zusatzleistung" : productName.trim();
        String reference = detail == null || detail.isBlank() ? "" : " „" + detail.trim() + "“";
        int safeDays = Math.max(days, 1);
        return product + reference + " auf nimmda.org, " + safeDays + " Tage";
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
