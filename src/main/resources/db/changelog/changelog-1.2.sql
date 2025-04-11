--liquibase formatted sql

--changeset zhavokhir:1.2.2
CREATE TABLE api_keys
(
    id          SERIAL PRIMARY KEY,
    key_hash    VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP,
    is_active   BOOLEAN   DEFAULT TRUE
);

--rollback DROP TABLE IF EXISTS api_keys;