--liquibase formatted sql

--changeset zhavokhir:1.4.1
alter table project add column slug VARCHAR(255) NOT NULL UNIQUE;
--rollback alter table project drop column slug;

--changeset zhavokhir:1.4.2
alter table catalog add column slug VARCHAR(255) NOT NULL UNIQUE;
--rollback alter table catalog drop column slug;