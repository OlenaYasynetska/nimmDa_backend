package com.nimmda.application.messaging;

import java.util.List;

public interface ListSellerConversationsUseCase {

    List<ConversationView> execute(String sellerId);
}
