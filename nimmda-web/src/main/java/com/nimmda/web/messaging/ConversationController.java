package com.nimmda.web.messaging;

import com.nimmda.application.messaging.ConversationView;
import com.nimmda.application.messaging.ListSellerConversationsUseCase;
import com.nimmda.application.messaging.ReplyToConversationUseCase;
import com.nimmda.application.messaging.SendInquiryCommand;
import com.nimmda.application.messaging.SendInquiryUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final SendInquiryUseCase sendInquiryUseCase;
    private final ListSellerConversationsUseCase listSellerConversationsUseCase;
    private final ReplyToConversationUseCase replyToConversationUseCase;

    public ConversationController(
            SendInquiryUseCase sendInquiryUseCase,
            ListSellerConversationsUseCase listSellerConversationsUseCase,
            ReplyToConversationUseCase replyToConversationUseCase
    ) {
        this.sendInquiryUseCase = sendInquiryUseCase;
        this.listSellerConversationsUseCase = listSellerConversationsUseCase;
        this.replyToConversationUseCase = replyToConversationUseCase;
    }

    @PostMapping("/listings/{listingId}/inquiries")
    public ConversationView sendInquiry(
            Authentication authentication,
            @PathVariable String listingId,
            @RequestBody(required = false) SendInquiryRequest request
    ) {
        String message = request == null ? null : request.message();
        return sendInquiryUseCase.execute(new SendInquiryCommand(
                listingId,
                authentication.getName(),
                message
        ));
    }

    @GetMapping("/conversations")
    public List<ConversationView> list(Authentication authentication) {
        return listSellerConversationsUseCase.execute(authentication.getName());
    }

    @PostMapping("/conversations/{id}/messages")
    public ConversationView reply(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ConversationReplyRequest request
    ) {
        return replyToConversationUseCase.execute(id, authentication.getName(), request.message());
    }
}
