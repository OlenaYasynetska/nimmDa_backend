package com.nimmda.application.port.mail;

public record MailMessage(String to, String subject, String htmlBody) {
}
