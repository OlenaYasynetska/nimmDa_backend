package com.nimmda.application.messaging;

import com.nimmda.application.listing.ListingNotFoundException;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.shared.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SendInquiryService implements SendInquiryUseCase {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;

    public SendInquiryService(
            ListingRepository listingRepository,
            ConversationRepository conversationRepository
    ) {
        this.listingRepository = listingRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    @Transactional
    public ConversationView execute(SendInquiryCommand command) {
        Listing listing = listingRepository
                .findById(new ListingId(command.listingId()))
                .orElseThrow(() -> new ListingNotFoundException(command.listingId()));
        if (!listing.isPublished()) {
            throw new ListingNotFoundException(command.listingId());
        }

        UserId buyerId = new UserId(command.buyerId());
        Conversation conversation = conversationRepository
                .findByListingAndBuyer(listing.id(), buyerId)
                .map(existing -> {
                    existing.addBuyerMessage(command.message());
                    return existing;
                })
                .orElseGet(() -> Conversation.start(
                        listing.id(),
                        listing.sellerId(),
                        buyerId,
                        command.buyerName(),
                        listing.title(),
                        command.message()
                ));

        Conversation saved = conversationRepository.save(conversation);
        listing.recordChat();
        listingRepository.save(listing);
        return ConversationMapper.toView(saved);
    }
}
