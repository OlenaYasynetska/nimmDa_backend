package com.nimmda.application.messaging;

import java.time.Instant;

public record MessageView(String author, String text, Instant createdAt) {
}
