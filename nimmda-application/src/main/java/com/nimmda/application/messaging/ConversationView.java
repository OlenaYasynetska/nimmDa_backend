package com.nimmda.application.messaging;

import java.time.Instant;
import java.util.List;

public record ConversationView(
        String id,
        String listingId,
        String participant,
        String listingTitle,
        String preview,
        Instant updatedAt,
        List<MessageView> messages
) {
}
