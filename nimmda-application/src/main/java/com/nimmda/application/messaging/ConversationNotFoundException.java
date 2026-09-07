package com.nimmda.application.messaging;

public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(String conversationId) {
        super("Conversation not found: " + conversationId);
    }
}
