CREATE TABLE favorites (
    user_id VARCHAR(36) NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (user_id, listing_id),
    INDEX idx_favorites_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
