package com.nimmda.application.messaging;

import com.nimmda.application.auth.AuthException;
import com.nimmda.application.listing.ListingNotFoundException;
import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.ConversationRepository;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SendInquiryService implements SendInquiryUseCase {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationViewAssembler views;

    public SendInquiryService(
            ListingRepository listingRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            ConversationViewAssembler views
    ) {
        this.listingRepository = listingRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.views = views;
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
        if (listing.sellerId().equals(buyerId)) {
            throw new ForbiddenActionException("Cannot inquire own listing");
        }
        User buyer = userRepository.findById(buyerId).orElseThrow(() -> new AuthException("notFound"));
        User seller = userRepository.findById(listing.sellerId()).orElseThrow(() -> new AuthException("notFound"));

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
                        ConversationViewAssembler.displayName(buyer),
                        ConversationViewAssembler.displayName(seller),
                        listing.title(),
                        command.message()
                ));

        Conversation saved = conversationRepository.save(conversation);
        listing.recordChat();
        listingRepository.save(listing);
        return views.toView(saved, command.buyerId());
    }
}
