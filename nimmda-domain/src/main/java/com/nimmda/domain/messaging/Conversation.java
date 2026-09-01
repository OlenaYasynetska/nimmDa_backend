package com.nimmda.domain.messaging;

import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.shared.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Conversation {

    private final ConversationId id;
    private final ListingId listingId;
    private final UserId sellerId;
    private final UserId buyerId;
    private final String buyerName;
    private final String listingTitle;
    private final List<Message> messages;
    private final Instant createdAt;
    private Instant updatedAt;

    private Conversation(
            ConversationId id,
            ListingId listingId,
            UserId sellerId,
            UserId buyerId,
            String buyerName,
            String listingTitle,
            List<Message> messages,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.listingId = Objects.requireNonNull(listingId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.buyerName = requireName(buyerName);
        this.listingTitle = requireTitle(listingTitle);
        this.messages = new ArrayList<>(Objects.requireNonNull(messages));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (this.messages.isEmpty()) {
            throw new IllegalArgumentException("conversation must have at least one message");
        }
    }

    public static Conversation start(
            ListingId listingId,
            UserId sellerId,
            UserId buyerId,
            String buyerName,
            String listingTitle,
            String firstMessage
    ) {
        Instant now = Instant.now();
        return new Conversation(
                ConversationId.newId(),
                listingId,
                sellerId,
                buyerId,
                buyerName,
                listingTitle,
                List.of(Message.fromBuyer(firstMessage)),
                now,
                now
        );
    }

    public static Conversation rehydrate(
            ConversationId id,
            ListingId listingId,
            UserId sellerId,
            UserId buyerId,
            String buyerName,
            String listingTitle,
            List<Message> messages,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Conversation(
                id,
                listingId,
                sellerId,
                buyerId,
                buyerName,
                listingTitle,
                messages,
                createdAt,
                updatedAt
        );
    }

    public void addBuyerMessage(String text) {
        messages.add(Message.fromBuyer(text));
        updatedAt = Instant.now();
    }

    public void addSellerMessage(String text) {
        messages.add(Message.fromSeller(text));
        updatedAt = Instant.now();
    }

    public String preview() {
        return messages.get(messages.size() - 1).text();
    }

    public ConversationId id() {
        return id;
    }

    public ListingId listingId() {
        return listingId;
    }

    public UserId sellerId() {
        return sellerId;
    }

    public UserId buyerId() {
        return buyerId;
    }

    public String buyerName() {
        return buyerName;
    }

    public String listingTitle() {
        return listingTitle;
    }

    public List<Message> messages() {
        return List.copyOf(messages);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "buyerName must not be null");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("buyerName must not be blank");
        }
        return normalized;
    }

    private static String requireTitle(String title) {
        Objects.requireNonNull(title, "listingTitle must not be null");
        String normalized = title.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("listingTitle must not be blank");
        }
        return normalized;
    }
}
