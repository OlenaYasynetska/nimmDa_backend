SET @ddl = IF (
    (SELECT COUNT(*)
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'listings'
       AND COLUMN_NAME = 'promoted_at') = 0,
    'ALTER TABLE listings ADD COLUMN promoted_at DATETIME(3) NULL',
    'SELECT 1'
);

PREPARE nimmda_stmt FROM @ddl;

EXECUTE nimmda_stmt;

DEALLOCATE PREPARE nimmda_stmt;

ALTER TABLE listing_promotions
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

UPDATE listings l
    JOIN (
        SELECT listing_id, MAX(paid_at) AS paid_at
        FROM listing_promotions
        WHERE status = 'PAID' AND paid_at IS NOT NULL
        GROUP BY listing_id
    ) p ON p.listing_id COLLATE utf8mb4_unicode_ci = l.id COLLATE utf8mb4_unicode_ci
SET l.promoted_at = p.paid_at
WHERE l.promoted_until IS NOT NULL;
