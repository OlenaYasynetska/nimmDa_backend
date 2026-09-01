package com.nimmda.application.messaging;

import java.time.Instant;
import java.util.List;

public record ConversationView(
        String id,
        String listingId,
        String sellerId,
        String buyerId,
        String buyerName,
        String listingTitle,
        String preview,
        Instant updatedAt,
        List<MessageView> messages
) {
}
