package com.nimmda.infrastructure.messaging;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationId;
import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.messaging.Message;
import com.nimmda.domain.messaging.MessageAuthor;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConversationRepositoryMongoAdapter implements ConversationRepository {

    private final SpringDataConversationMongoRepository mongoRepository;

    public ConversationRepositoryMongoAdapter(SpringDataConversationMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        return toDomain(mongoRepository.save(toDocument(conversation)));
    }

    @Override
    public Optional<Conversation> findById(ConversationId conversationId) {
        return mongoRepository.findById(conversationId.value()).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByListingAndBuyer(ListingId listingId, UserId buyerId) {
        return mongoRepository
                .findByListingIdAndBuyerId(listingId.value(), buyerId.value())
                .map(this::toDomain);
    }

    @Override
    public List<Conversation> findBySellerId(UserId sellerId) {
        return mongoRepository.findBySellerIdOrderByUpdatedAtDesc(sellerId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Conversation> findByBuyerId(UserId buyerId) {
        return mongoRepository.findByBuyerIdOrderByUpdatedAtDesc(buyerId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private ConversationDocument toDocument(Conversation conversation) {
        ConversationDocument document = new ConversationDocument();
        document.setId(conversation.id().value());
        document.setListingId(conversation.listingId().value());
        document.setSellerId(conversation.sellerId().value());
        document.setBuyerId(conversation.buyerId().value());
        document.setBuyerName(conversation.buyerName());
        document.setListingTitle(conversation.listingTitle());
        document.setMessages(conversation.messages().stream()
                .map(message -> new MessageDocument(
                        message.author().name(),
                        message.text(),
                        message.createdAt()
                ))
                .toList());
        document.setCreatedAt(conversation.createdAt());
        document.setUpdatedAt(conversation.updatedAt());
        return document;
    }

    private Conversation toDomain(ConversationDocument document) {
        List<Message> messages = document.getMessages().stream()
                .map(item -> new Message(
                        MessageAuthor.valueOf(item.getAuthor()),
                        item.getText(),
                        item.getCreatedAt()
                ))
                .toList();
        return Conversation.rehydrate(
                new ConversationId(document.getId()),
                new ListingId(document.getListingId()),
                new UserId(document.getSellerId()),
                new UserId(document.getBuyerId()),
                document.getBuyerName(),
                document.getListingTitle(),
                messages,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
