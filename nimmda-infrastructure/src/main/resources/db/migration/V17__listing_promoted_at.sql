ALTER TABLE listings
    ADD COLUMN promoted_at DATETIME(3) NULL;

UPDATE listings l
    JOIN (
        SELECT listing_id, MAX(paid_at) AS paid_at
        FROM listing_promotions
        WHERE status = 'PAID' AND paid_at IS NOT NULL
        GROUP BY listing_id
    ) p ON p.listing_id = l.id
SET l.promoted_at = p.paid_at
WHERE l.promoted_until IS NOT NULL;
