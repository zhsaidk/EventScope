--liquibase formatted sql

--changeset zhavokhir:1.0
CREATE TABLE project
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    active      BOOLEAN   default true,
    created_at  timestamp default now()
);
--rollback drop table if exists project;

--changeset zhavokhir:1.1
CREATE TABLE catalog
(
    id          SERIAL PRIMARY KEY,
    project_id  INT references project (id) on delete cascade ,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    active      BOOLEAN   default true,
    version     VARCHAR(255),
    created_at  timestamp default now()
);
--rollback drop table if exists catalog;

--changeset zhavokhir:1.2
CREATE TABLE event
(
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_id       INT references catalog (id) on delete cascade ,
    name             VARCHAR(255),
    parameters       jsonb,
    local_created_at timestamp,
    created_at       timestamp default now()
);
--rollback drop table if exists event