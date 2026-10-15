CREATE TABLE business_profiles (
    user_id VARCHAR(36) NOT NULL,
    company_name VARCHAR(160) NOT NULL,
    trade VARCHAR(160) NOT NULL,
    location VARCHAR(128) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    active_until DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE business_subscriptions (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    amount_cents INT NOT NULL,
    currency CHAR(3) NOT NULL,
    days INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    provider_session_id VARCHAR(255) NULL,
    active_until DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL,
    paid_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_subscriptions_session (provider_session_id),
    KEY idx_business_subscriptions_paid (status, paid_at)
);
