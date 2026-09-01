package com.nimmda.domain.messaging;

import java.util.Objects;
import java.util.UUID;

public record ConversationId(String value) {

    public ConversationId {
        Objects.requireNonNull(value, "conversationId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        value = value.trim();
    }

    public static ConversationId newId() {
        return new ConversationId(UUID.randomUUID().toString());
    }
}
