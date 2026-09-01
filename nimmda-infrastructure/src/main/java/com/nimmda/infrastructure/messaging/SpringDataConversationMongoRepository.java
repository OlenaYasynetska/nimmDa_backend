package com.nimmda.infrastructure.messaging;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataConversationMongoRepository extends MongoRepository<ConversationDocument, String> {

    Optional<ConversationDocument> findByListingIdAndBuyerId(String listingId, String buyerId);

    List<ConversationDocument> findBySellerIdOrderByUpdatedAtDesc(String sellerId);

    List<ConversationDocument> findByBuyerIdOrderByUpdatedAtDesc(String buyerId);
}
