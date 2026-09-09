DELETE t FROM auth_tokens t
INNER JOIN users u ON u.id = t.user_id
WHERE u.email = 'sharlot07870@gmail.com';

DELETE FROM users WHERE email = 'sharlot07870@gmail.com';

UPDATE users SET role = 'USER' WHERE role = 'ADMIN';
