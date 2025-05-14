--liquibase formatted sql

--changeset zhavokhir:1.9.1
CREATE TABLE project_permission(
    id SERIAL PRIMARY KEY ,
    user_id INTEGER REFERENCES postgres.public.users(id) ON DELETE CASCADE NOT NULL ,
    project_id INTEGER REFERENCES postgres.public.project(id) ON DELETE SET NULL ,
    permission VARCHAR(12) NOT NULL ,
    created_at TIMESTAMP NOT NULL
);
--rollback ALTER TABLE project DROP COLUMN access_permission;