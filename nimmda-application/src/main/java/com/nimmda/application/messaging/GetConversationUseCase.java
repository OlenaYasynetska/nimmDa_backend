package com.nimmda.application.messaging;

public interface GetConversationUseCase {

    ConversationView execute(String conversationId, String userId);
}
