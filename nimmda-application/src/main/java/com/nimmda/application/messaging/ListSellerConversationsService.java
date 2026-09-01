package com.nimmda.application.messaging;

import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSellerConversationsService implements ListSellerConversationsUseCase {

    private final ConversationRepository conversationRepository;

    public ListSellerConversationsService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> execute(String sellerId) {
        return conversationRepository.findBySellerId(new UserId(sellerId)).stream()
                .map(ConversationMapper::toView)
                .toList();
    }
}
