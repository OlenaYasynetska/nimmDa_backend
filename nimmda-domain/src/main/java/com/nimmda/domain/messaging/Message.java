package com.nimmda.domain.messaging;

import java.time.Instant;
import java.util.Objects;

public record Message(MessageAuthor author, String text, Instant createdAt) {

    public Message {
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        String normalized = text.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        text = normalized;
    }

    public static Message fromBuyer(String text) {
        return new Message(MessageAuthor.BUYER, text, Instant.now());
    }

    public static Message fromSeller(String text) {
        return new Message(MessageAuthor.SELLER, text, Instant.now());
    }
}
