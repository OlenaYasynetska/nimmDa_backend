CREATE TABLE listings (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    category VARCHAR(128) NOT NULL,
    location VARCHAR(128) NOT NULL,
    image_src VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL,
    views INT NOT NULL DEFAULT 0,
    chats INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    INDEX idx_listings_status_created (status, created_at),
    INDEX idx_listings_category (category),
    INDEX idx_listings_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
