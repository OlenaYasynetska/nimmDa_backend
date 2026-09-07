package com.nimmda.application.messaging;

public interface ReplyToConversationUseCase {

    ConversationView execute(String conversationId, String userId, String message);
}
