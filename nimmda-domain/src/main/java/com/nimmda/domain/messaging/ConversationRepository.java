package com.nimmda.domain.messaging;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.shared.UserId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(ConversationId conversationId);

    Optional<Conversation> findByListingAndBuyer(ListingId listingId, UserId buyerId);

    List<Conversation> findBySellerId(UserId sellerId);

    List<Conversation> findByBuyerId(UserId buyerId);
}
