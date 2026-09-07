package com.nimmda.web.messaging;

import com.nimmda.application.messaging.ConversationView;
import com.nimmda.application.messaging.ListSellerConversationsUseCase;
import com.nimmda.application.messaging.ReplyToConversationUseCase;
import com.nimmda.application.messaging.SendInquiryCommand;
import com.nimmda.application.messaging.SendInquiryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationView sendInquiry(
            Authentication authentication,
            @PathVariable String listingId,
            @Valid @RequestBody SendInquiryRequest request
    ) {
        return sendInquiryUseCase.execute(new SendInquiryCommand(
                listingId,
                authentication.getName(),
                request.message()
        ));
    }

    @GetMapping("/conversations")
    public List<ConversationView> listForSeller(Authentication authentication) {
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
