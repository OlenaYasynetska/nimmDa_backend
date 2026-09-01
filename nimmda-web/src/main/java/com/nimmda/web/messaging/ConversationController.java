package com.nimmda.web.messaging;

import com.nimmda.application.messaging.ConversationView;
import com.nimmda.application.messaging.ListSellerConversationsUseCase;
import com.nimmda.application.messaging.SendInquiryCommand;
import com.nimmda.application.messaging.SendInquiryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final SendInquiryUseCase sendInquiryUseCase;
    private final ListSellerConversationsUseCase listSellerConversationsUseCase;

    public ConversationController(
            SendInquiryUseCase sendInquiryUseCase,
            ListSellerConversationsUseCase listSellerConversationsUseCase
    ) {
        this.sendInquiryUseCase = sendInquiryUseCase;
        this.listSellerConversationsUseCase = listSellerConversationsUseCase;
    }

    @PostMapping("/listings/{listingId}/inquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationView sendInquiry(
            @PathVariable String listingId,
            @Valid @RequestBody SendInquiryRequest request
    ) {
        return sendInquiryUseCase.execute(new SendInquiryCommand(
                listingId,
                request.buyerId(),
                request.buyerName(),
                request.message()
        ));
    }

    @GetMapping("/conversations")
    public List<ConversationView> listForSeller(@RequestParam String sellerId) {
        return listSellerConversationsUseCase.execute(sellerId);
    }
}
