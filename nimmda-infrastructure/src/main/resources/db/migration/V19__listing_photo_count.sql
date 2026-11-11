ALTER TABLE listings
    ADD COLUMN photo_count INT NOT NULL DEFAULT 1;

UPDATE listings
SET photo_count = LEAST(JSON_LENGTH(images), 5)
WHERE images IS NOT NULL
  AND JSON_VALID(images)
  AND JSON_LENGTH(images) > 0;
