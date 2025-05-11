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