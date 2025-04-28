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