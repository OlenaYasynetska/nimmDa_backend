package com.nimmda.web.messaging;

import jakarta.validation.constraints.NotBlank;

public record ConversationReplyRequest(@NotBlank String message) {
}
