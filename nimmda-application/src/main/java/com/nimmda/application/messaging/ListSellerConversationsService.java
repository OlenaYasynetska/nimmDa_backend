package com.nimmda.application.messaging;

import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListSellerConversationsService implements ListSellerConversationsUseCase {

    private final ConversationRepository conversationRepository;
    private final ConversationViewAssembler views;

    public ListSellerConversationsService(
            ConversationRepository conversationRepository,
            ConversationViewAssembler views
    ) {
        this.conversationRepository = conversationRepository;
        this.views = views;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> execute(String userId) {
        UserId id = new UserId(userId);
        Map<String, Conversation> unique = new LinkedHashMap<>();
        for (Conversation conversation : conversationRepository.findBySellerId(id)) {
            unique.put(conversation.id().value(), conversation);
        }
        for (Conversation conversation : conversationRepository.findByBuyerId(id)) {
            unique.put(conversation.id().value(), conversation);
        }
        return unique.values().stream()
                .sorted(Comparator.comparing(Conversation::updatedAt).reversed())
                .map(conversation -> views.toView(conversation, userId))
                .toList();
    }
}
