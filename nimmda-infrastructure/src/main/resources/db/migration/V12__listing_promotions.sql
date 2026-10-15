ALTER TABLE listings
    ADD COLUMN promoted_until DATETIME(3) NULL;

CREATE TABLE listing_promotions (
    id VARCHAR(36) NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    amount_cents INT NOT NULL,
    currency CHAR(3) NOT NULL,
    days INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    provider_session_id VARCHAR(255) NULL,
    promoted_until DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL,
    paid_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_listing_promotions_session (provider_session_id),
    KEY idx_listing_promotions_paid (status, paid_at)
);
