package com.nimmda.application.mail;

public record SendAuthMailCommand(String to, String type, String link) {
}
