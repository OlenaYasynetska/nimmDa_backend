package com.nimmda.application.messaging;

import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.messaging.MessageAuthor;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ConversationViewAssembler {

    private final UserRepository userRepository;

    public ConversationViewAssembler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ConversationView toView(Conversation conversation, String viewerId) {
        UserId viewer = new UserId(viewerId);
        boolean viewerIsSeller = conversation.sellerIs(viewer);
        String participant = viewerIsSeller
                ? conversation.buyerName()
                : sellerDisplayName(conversation);
        return new ConversationView(
                conversation.id().value(),
                conversation.listingId().value(),
                participant,
                conversation.listingTitle(),
                conversation.preview(),
                conversation.updatedAt(),
                conversation.messages().stream()
                        .map(message -> new MessageView(
                                isOwnMessage(viewerIsSeller, message.author()) ? "self" : "other",
                                message.text(),
                                message.createdAt()
                        ))
                        .toList()
        );
    }

    private String sellerDisplayName(Conversation conversation) {
        if (!conversation.sellerName().isBlank()) {
            return conversation.sellerName();
        }
        return userRepository.findById(conversation.sellerId())
                .map(ConversationViewAssembler::displayName)
                .orElse("Mitglied");
    }

    private static boolean isOwnMessage(boolean viewerIsSeller, MessageAuthor author) {
        return viewerIsSeller ? author == MessageAuthor.SELLER : author == MessageAuthor.BUYER;
    }

    static String displayName(User user) {
        String last = user.lastName();
        if (last == null || last.isBlank()) {
            return user.firstName();
        }
        return user.firstName() + " " + last.charAt(0) + ".";
    }
}
