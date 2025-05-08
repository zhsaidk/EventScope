--liquibase formatted sql

--changeset zhavokhir:1.8.1
ALTER TABLE project
    ADD COLUMN owner_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE;

--rollback ALTER TABLE project DROP COLUMN owner_id;