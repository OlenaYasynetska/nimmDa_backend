CREATE TABLE homepage_banner (
    id VARCHAR(16) NOT NULL,
    advertiser VARCHAR(160) NOT NULL,
    image_src MEDIUMTEXT NULL,
    target_url VARCHAR(500) NULL,
    starts_on DATE NULL,
    ends_on DATE NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id)
);
