-- PostgreSQL Schema for ChronoBank System


-- DROP TABLE IF EXISTS investments CASCADE;
-- DROP TABLE IF EXISTS transaction_log CASCADE;
-- DROP TABLE IF EXISTS transactions CASCADE;
-- DROP TABLE IF EXISTS accounts CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    user_id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    reputation_score INT DEFAULT 0,
    risk_score INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Accounts Table
CREATE TABLE accounts (
    account_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_type VARCHAR(50) NOT NULL, -- e.g., 'BASIC', 'INVESTOR', 'LOAN'
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL, -- e.g., 'ACTIVE', 'OVERDRAWN', 'FROZEN'
    preferences_json JSONB, -- For AccountPreferences (transactionLimitPerDay, notificationPreferences, etc.)
    creation_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Fields specific to LoanAccount
    loan_amount DECIMAL(15,2) NULL,
    loan_interest_rate DECIMAL(5,4) NULL, -- e.g., 0.05 for 5%
    loan_due_date TIMESTAMP WITH TIME ZONE NULL,
    loan_repayment_strategy VARCHAR(100) NULL, -- Name of the strategy class or identifier
    -- Fields specific to InvestorAccount
    investor_interest_rate DECIMAL(5,4) NULL -- e.g., 0.03 for 3%
);

-- Transactions Table
CREATE TABLE transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL, -- e.g., 'TRANSFER', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT', 'INVESTMENT_NEW', 'INVESTMENT_DIVEST', 'INTEREST_ACCRUAL', 'FEE_CHARGE'
    from_account_id VARCHAR(255) REFERENCES accounts(account_id) ON DELETE SET NULL NULL, -- SET NULL if account is deleted, or handle appropriately
    to_account_id VARCHAR(255) REFERENCES accounts(account_id) ON DELETE SET NULL NULL,
    amount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL, -- e.g., 'PENDING', 'COMPLETED', 'FAILED', 'REVERTED'
    description TEXT NULL, -- e.g., reason for failure, details of transaction
    related_investment_id VARCHAR(255) NULL -- Link to an investment if applicable
);

-- Transaction Log Table (for ChronoLedger, can be more detailed for audit)
-- This can be a more detailed audit trail if needed, or ChronoLedger can directly query 'transactions' table.
-- For simplicity, we'll assume ChronoLedger primarily uses the 'transactions' table.
-- If a separate, immutable log is strictly required by Singleton pattern's intent:
CREATE TABLE transaction_log (
    log_id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    logged_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    event_description TEXT NOT NULL, -- e.g., 'Transaction initiated', 'Transaction completed', 'Attempted undo'
    snapshot_details JSONB NULL -- Optional: JSON snapshot of relevant data at the time of logging
);

-- Investments Table (For InvestorAccount)
CREATE TABLE investments (
    investment_id VARCHAR(255) PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    investment_type VARCHAR(100) NOT NULL, -- e.g., 'TIME_BONDS', 'FUTURE_PROJECT_X'
    amount_invested DECIMAL(15,2) NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    maturity_date TIMESTAMP WITH TIME ZONE NULL, -- For investments with a defined end
    current_value DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., 'ACTIVE', 'MATURED', 'SOLD', 'CANCELLED'
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);



-- Admin table
CREATE TABLE IF NOT EXISTS admins (
    admin_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User reputation table
CREATE TABLE IF NOT EXISTS user_reputation (
    user_id VARCHAR(50) PRIMARY KEY REFERENCES users(user_id),
    reputation_score INTEGER DEFAULT 100,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fraud_flags INTEGER DEFAULT 0,
    CONSTRAINT valid_reputation_score CHECK (reputation_score >= 0 AND reputation_score <= 1000)
);

-- Admin audit log
CREATE TABLE IF NOT EXISTS admin_audit_log (
    audit_id SERIAL PRIMARY KEY,
    admin_id VARCHAR(50) REFERENCES admins(admin_id),
    action_type VARCHAR(50) NOT NULL,
    target_user_id VARCHAR(50) REFERENCES users(user_id),
    action_details TEXT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin account
INSERT INTO admins (admin_id, username, password_hash, email)
VALUES ('ADMIN001', 'admin', '$2a$10$8K1p/a0dR1xqM8K3hQz1eOQZQZQZQZQZQZQZQZQZQZQZQZQZQZQZ', 'admin@chronobank.com')
ON CONFLICT (username) DO NOTHING;

-- Add reputation score to existing users
INSERT INTO user_reputation (user_id, reputation_score)
SELECT user_id, 100 FROM users
ON CONFLICT (user_id) DO NOTHING;

-- Indexes for performance
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_from_account_id ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account_id ON transactions(to_account_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX idx_investments_account_id ON investments(account_id);
