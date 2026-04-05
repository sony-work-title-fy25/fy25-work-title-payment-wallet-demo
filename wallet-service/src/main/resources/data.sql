-- Seed test user and wallet for development
INSERT INTO users (email, password, first_name, last_name, phone_number, status, created_at, updated_at)
VALUES ('user@example.com', '$2a$10$dummyhashedpasswordfordevonly000000000000000000', 'Test', 'User', '+1234567890', 'ACTIVE', NOW(), NOW());

INSERT INTO wallets (user_id, wallet_id, balance, currency, status, version, created_at, updated_at)
VALUES (1, 'WAL-TEST-001', 1000.00, 'USD', 'ACTIVE', 0, NOW(), NOW());
