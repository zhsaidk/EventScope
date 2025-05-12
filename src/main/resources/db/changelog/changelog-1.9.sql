--liquibase formatted sql

--changeset zhavokhir:1.9.1
CREATE TABLE acl_class (
                           id BIGSERIAL PRIMARY KEY,
                           class VARCHAR(100) NOT NULL,
                           class_id_type VARCHAR(255),
                           CONSTRAINT uk_acl_class UNIQUE (class)
); -- rollback DROP TABLE acl_class;
--changeset zhavokhir:1.9.2
CREATE TABLE acl_sid (
                         id BIGSERIAL PRIMARY KEY,
                         principal BOOLEAN NOT NULL,
                         sid VARCHAR(100) NOT NULL,
                         CONSTRAINT uk_acl_sid UNIQUE (sid, principal)
); -- rollback DROP TABLE acl_sid;

--changeset zhavokhir:1.9.3
CREATE TABLE acl_object_identity (
                                     id BIGSERIAL PRIMARY KEY,
                                     object_id_class BIGINT NOT NULL,
                                     object_id_identity VARCHAR(255) NOT NULL, -- Изменено с BIGINT на VARCHAR
                                     parent_object BIGINT,
                                     owner_sid BIGINT,
                                     entries_inheriting BOOLEAN NOT NULL,
                                     CONSTRAINT uk_acl_obj_id UNIQUE (object_id_class, object_id_identity),
                                     CONSTRAINT fk_acl_obj_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
                                     CONSTRAINT fk_acl_obj_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
                                     CONSTRAINT fk_acl_obj_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
); --rollback DROP TABLE acl_object_identity;
--changeset zhavokhir:1.9.4
CREATE TABLE acl_entry (
                           id BIGSERIAL PRIMARY KEY,
                           acl_object_identity BIGINT NOT NULL,
                           ace_order INT NOT NULL,
                           sid BIGINT NOT NULL,
                           mask INT NOT NULL,
                           granting BOOLEAN NOT NULL,
                           audit_success BOOLEAN NOT NULL,
                           audit_failure BOOLEAN NOT NULL,
                           CONSTRAINT uk_acl_entry UNIQUE (acl_object_identity, ace_order),
                           CONSTRAINT fk_acl_entry_obj FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
                           CONSTRAINT fk_acl_entry_sid FOREIGN KEY (sid) REFERENCES acl_sid (id)
); --rollback DROP TABLE acl_entry;