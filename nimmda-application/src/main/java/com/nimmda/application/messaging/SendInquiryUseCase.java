package com.nimmda.application.messaging;

public interface SendInquiryUseCase {

    ConversationView execute(SendInquiryCommand command);
}
