--liquibase formatted sql

--changeset zhavokhir:1.7.1
create table users(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    username VARCHAR(124) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL CHECK ( role in ('ADMIN', 'USER', 'GUEST'))
);
--rollback drop table if exists users;

--changeset zhavokhir:1.7.2
alter table api_keys
add column user_id INTEGER REFERENCES users(id) on delete cascade ;
--rollback alter table api_keys drop column user_id;

--liquibase formatted sql

--changeset zhavokhir:1.7.3
INSERT INTO users(name, username, password, role) VALUES
                                                      ('User', 'user@gmail.com', '{noop}123', 'ADMIN'),
                                                      ('Admin', 'admin@gmail.com', '{noop}123', 'ADMIN'),
                                                      ('test', 'test@gmail.com', '{noop}123', 'ADMIN');
--rollback delete from users where username = 'admin@gmail.com'

--changeset zhavokhir:1.7.4
INSERT INTO api_keys(key_hash, description, created_at, expires_at, is_active, user_id)
VALUES ('3uE9j1Vf9TBLMCwyrWtrs3tMGBXwQTbE2DV4w', 'Дано для доступа к эндпоинтам', '2025-04-11 18:25:52.786146', '2026-04-11 18:25:52.734933', true, 1);
--rollback delete from api_keys where key_hash = '3uE9j1Vf9TBLMCwyrWtrs3tMGBXwQTbE2DV4w'