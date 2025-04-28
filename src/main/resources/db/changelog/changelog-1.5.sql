--changeset zhavokhir:1.5.1
CREATE TRIGGER catalog_slug_trigger
    BEFORE INSERT
    ON catalog
    FOR EACH ROW
    EXECUTE FUNCTION set_slug_function();
--rollback DROP TRIGGER catalog_slug_trigger ON catalog;

--changeset zhavokhir:1.5.2
CREATE TRIGGER project_slug_trigger
    BEFORE INSERT
    ON project
    FOR EACH ROW
    EXECUTE FUNCTION set_slug_function();
--rollback DROP TRIGGER project_slug_trigger ON catalog;