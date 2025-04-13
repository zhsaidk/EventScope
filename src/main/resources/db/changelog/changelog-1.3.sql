--liquibase formatted sql

--changeset zhavokhir:1.3.1
INSERT INTO api_keys(key_hash, description, created_at, expires_at, is_active)
VALUES ('3uE9j1Vf9TBLMCwyrWtrs3tMGBXwQTbE2DV4w', 'Дано для доступа к эндпоинтам', '2025-04-11 18:25:52.786146', '2026-04-11 18:25:52.734933', false);
--rollback delete from api_keys where key_hash = '3uE9j1Vf9TBLMCwyrWtrs3tMGBXwQTbE2DV4w'