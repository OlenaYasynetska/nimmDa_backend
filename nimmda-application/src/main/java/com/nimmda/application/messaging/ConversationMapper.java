package com.nimmda.application.messaging;

import com.nimmda.domain.messaging.Conversation;

final class ConversationMapper {

    private ConversationMapper() {
    }

    static ConversationView toView(Conversation conversation) {
        return new ConversationView(
                conversation.id().value(),
                conversation.listingId().value(),
                conversation.sellerId().value(),
                conversation.buyerId().value(),
                conversation.buyerName(),
                conversation.listingTitle(),
                conversation.preview(),
                conversation.updatedAt(),
                conversation.messages().stream()
                        .map(message -> new MessageView(
                                message.author().name().toLowerCase(),
                                message.text(),
                                message.createdAt()
                        ))
                        .toList()
        );
    }
}
