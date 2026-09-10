UPDATE users
SET account_mode = 'BOTH',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE account_mode <> 'BOTH';
