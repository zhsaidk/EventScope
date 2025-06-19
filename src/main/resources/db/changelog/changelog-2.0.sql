--liquibase formatted sql

--changeset zhavokhir:2.0.1
-- Индекс для JOIN и фильтрации по catalog_id
CREATE INDEX inx_event_catalog_id ON event(catalog_id);
--rollback drop index inx_event_catalog_id;

--changeset zhavokhir:2.0.2
-- Индекс для поиска по имени (с поддержкой LOWER для LIKE)
CREATE INDEX idx_event_name_lower ON event(lower(name));
--rollback DROP INDEX idx_event_name_lower;

--changeset zhavokhir:2.0.3
-- Индекс для диапазонных запросов по local_created_at
CREATE INDEX idx_event_local_created_at ON event(local_created_at);
--rollback DROP INDEX idx_event_local_created_at;

--changeset zhavokhir:2.0.4
-- Индекс для JOIN с project
CREATE INDEX idx_catalog_project_id ON catalog(project_id);
--rollback DROP INDEX idx_catalog_project_id;

--changeset zhavokhir:2.0.5
-- Индекс для фильтрации по slug
CREATE INDEX idx_catalog_slug ON catalog(slug);
--rollback DROP INDEX idx_catalog_slug;

--changeset zhavokhir:2.0.6
-- Индекс для фильтрации по slug
CREATE INDEX idx_project_slug ON project(slug);
--rollback DROP INDEX idx_project_slug;

--changeset zhavokhir:2.0.7
-- Индекс для фильтрации по owner_id (если используется)
CREATE INDEX idx_project_owner_id ON project(owner_id);
--rollback DROP INDEX idx_project_owner_id;

--changeset zhavokhir:2.0.8
-- Индекс для JOIN и фильтрации по project_id
CREATE INDEX idx_project_permission_project_id ON project_permission(project_id);
--rollback DROP INDEX idx_project_permission_project_id;

--changeset zhavokhir:2.0.9
-- Индекс для фильтрации по user_id
CREATE INDEX idx_project_permission_user_id ON project_permission(user_id);
--rollback DROP INDEX idx_project_permission_user_id;

--changeset zhavokhir:2.0.10
-- Составной индекс для типичного фильтра
CREATE INDEX idx_project_permission_id_permission ON project_permission(user_id, permission);
--rollback DROP INDEX idx_project_permission_id_permission;