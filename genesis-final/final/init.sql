CREATE DATABASE IF NOT EXISTS genesis_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE genesis_db;

CREATE TABLE IF NOT EXISTS plans (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    tokens_granted  INT          NOT NULL,
    description     VARCHAR(255),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(80)  NOT NULL,
    role            ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    token_balance   INT          NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    plan_id         BIGINT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS catalog_operations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    base_cost   INT          NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    cop_per_usd DECIMAL(12,2) NOT NULL,
    updated_by  BIGINT,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_user FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    operation_id    BIGINT       NOT NULL,
    tokens_input    INT          NOT NULL DEFAULT 0,
    tokens_output   INT          NOT NULL DEFAULT 0,
    base_cost       INT          NOT NULL,
    total_cost      INT          NOT NULL DEFAULT 0,
    balance_before  INT          NOT NULL,
    balance_after   INT          NOT NULL,
    status          ENUM('SUCCESS','FAILED_INSUFFICIENT_TOKENS','FAILED_OPERATION_ERROR') NOT NULL,
    error_message   VARCHAR(500),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user      FOREIGN KEY (user_id)      REFERENCES users(id),
    CONSTRAINT fk_tx_operation FOREIGN KEY (operation_id) REFERENCES catalog_operations(id)
);

CREATE INDEX IF NOT EXISTS idx_tx_user_id    ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_tx_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_tx_status     ON transactions(status);
