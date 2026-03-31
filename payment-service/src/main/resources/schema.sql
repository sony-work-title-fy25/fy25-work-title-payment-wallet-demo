-- Payment Gateway Database Schema
-- PostgreSQL

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    merchant_reference VARCHAR(100),
    external_transaction_id VARCHAR(100),
    payment_provider VARCHAR(50),
    payment_method_id UUID,
    failure_reason VARCHAR(500),
    product_id VARCHAR(100),
    product_type VARCHAR(50),
    platform VARCHAR(50),
    region VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for transactions
CREATE INDEX IF NOT EXISTS idx_transaction_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transaction_external_id ON transactions(external_transaction_id);

-- Create payment_methods table
CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    nickname VARCHAR(100),
    provider_token VARCHAR(200) NOT NULL,
    last_four_digits VARCHAR(4),
    card_brand VARCHAR(50),
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    billing_name VARCHAR(100),
    billing_address VARCHAR(200),
    billing_city VARCHAR(100),
    billing_state VARCHAR(50),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(3),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for payment_methods
CREATE INDEX IF NOT EXISTS idx_payment_method_user_id ON payment_methods(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_method_type ON payment_methods(type);

-- Add foreign key constraint
ALTER TABLE transactions 
    ADD CONSTRAINT fk_transaction_payment_method 
    FOREIGN KEY (payment_method_id) 
    REFERENCES payment_methods(id);

-- Create refunds table
CREATE TABLE IF NOT EXISTS refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(30) NOT NULL,
    reason_details VARCHAR(500),
    external_refund_id VARCHAR(100),
    failure_reason VARCHAR(500),
    processed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_refund_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

-- Create indexes for refunds
CREATE INDEX IF NOT EXISTS idx_refund_transaction_id ON refunds(transaction_id);
CREATE INDEX IF NOT EXISTS idx_refund_user_id ON refunds(user_id);
CREATE INDEX IF NOT EXISTS idx_refund_status ON refunds(status);
