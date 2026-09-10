package com.nimmda.application.messaging;

import com.nimmda.application.security.ForbiddenActionException;
import com.nimmda.domain.messaging.Conversation;
import com.nimmda.domain.shared.UserId;

public final class ConversationAuthorization {

    private ConversationAuthorization() {
    }

    public static Conversation requireParticipant(Conversation conversation, String userId) {
        if (!conversation.includes(new UserId(userId))) {
            throw new ForbiddenActionException("Not a participant");
        }
        return conversation;
    }
}
