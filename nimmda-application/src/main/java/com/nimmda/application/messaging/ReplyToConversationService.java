package com.nimmda.application.messaging;

import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationId;
import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplyToConversationService implements ReplyToConversationUseCase {

    private final ConversationRepository conversationRepository;
    private final ConversationViewAssembler views;

    public ReplyToConversationService(
            ConversationRepository conversationRepository,
            ConversationViewAssembler views
    ) {
        this.conversationRepository = conversationRepository;
        this.views = views;
    }

    @Override
    @Transactional
    public ConversationView execute(String conversationId, String userId, String message) {
        Conversation conversation = conversationRepository
                .findById(new ConversationId(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        UserId actor = new UserId(userId);
        if (conversation.sellerId().equals(actor)) {
            conversation.addSellerMessage(message);
        } else if (conversation.buyerId().equals(actor)) {
            conversation.addBuyerMessage(message);
        } else {
            throw new ForbiddenActionException("Not a participant");
        }
        return views.toView(conversationRepository.save(conversation), userId);
    }
}
