--liquibase formatted sql

--changeset zhavokhir:1.6.1
alter table event
add column time INTEGER NULL ;

--rollback alter table event drop column time;