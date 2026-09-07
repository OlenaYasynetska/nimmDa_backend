package com.nimmda.infrastructure.mail;

import com.nimmda.application.port.mail.MailMessage;
import com.nimmda.application.port.mail.MailSender;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RoutingMailSender implements MailSender {

    private final ResendMailSenderAdapter resend;
    private final SmtpMailSenderAdapter smtp;

    public RoutingMailSender(ResendMailSenderAdapter resend, SmtpMailSenderAdapter smtp) {
        this.resend = resend;
        this.smtp = smtp;
    }

    @Override
    public boolean send(MailMessage message) {
        if (resend.configured()) {
            return resend.send(message);
        }
        return smtp.send(message);
    }

    @Override
    public boolean configured() {
        return resend.configured() || smtp.configured();
    }

    @Override
    public String provider() {
        if (resend.configured()) {
            return "resend";
        }
        if (smtp.configured()) {
            return "smtp";
        }
        return "none";
    }
}
