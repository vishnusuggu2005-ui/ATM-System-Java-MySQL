CREATE DATABASE IF NOT EXISTS atm_db;
USE atm_db;

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    pin VARCHAR(4) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CHECK (balance >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    CHECK (amount > 0)
) ENGINE=InnoDB;

-- Demo account: account number 1001001, PIN 1234.
-- This is for local learning only. Do not store real PINs as plain text.
INSERT INTO accounts (account_number, holder_name, pin, balance)
VALUES ('1001001', 'Demo User', '1234', 5000.00)
ON DUPLICATE KEY UPDATE holder_name = VALUES(holder_name);
