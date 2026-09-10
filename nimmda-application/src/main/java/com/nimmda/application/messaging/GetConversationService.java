package com.nimmda.application.messaging;

import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationId;
import com.nimmda.domain.messaging.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetConversationService implements GetConversationUseCase {

    private final ConversationRepository conversationRepository;
    private final ConversationViewAssembler views;

    public GetConversationService(
            ConversationRepository conversationRepository,
            ConversationViewAssembler views
    ) {
        this.conversationRepository = conversationRepository;
        this.views = views;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationView execute(String conversationId, String userId) {
        Conversation conversation = conversationRepository
                .findById(new ConversationId(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        ConversationAuthorization.requireParticipant(conversation, userId);
        return views.toView(conversation, userId);
    }
}
