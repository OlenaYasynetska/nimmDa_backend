package com.nimmda.application.port.mail;

public interface MailSender {

    boolean send(MailMessage message);
}
