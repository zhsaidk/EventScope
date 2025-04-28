--liquibase formatted sql
--changeset zhavokhir:1.4.1 endDelimiter:GO
CREATE FUNCTION set_slug_function() RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
DECLARE
    temp_slug TEXT;
BEGIN
    IF NEW.name IS NOT NULL THEN
        temp_slug := LOWER(NEW.name);

        temp_slug := REPLACE(temp_slug, 'а', 'a');
        temp_slug := REPLACE(temp_slug, 'б', 'b');
        temp_slug := REPLACE(temp_slug, 'в', 'v');
        temp_slug := REPLACE(temp_slug, 'г', 'g');
        temp_slug := REPLACE(temp_slug, 'д', 'd');
        temp_slug := REPLACE(temp_slug, 'е', 'e');
        temp_slug := REPLACE(temp_slug, 'ё', 'e');
        temp_slug := REPLACE(temp_slug, 'ж', 'zh');
        temp_slug := REPLACE(temp_slug, 'з', 'z');
        temp_slug := REPLACE(temp_slug, 'и', 'i');
        temp_slug := REPLACE(temp_slug, 'й', 'y');
        temp_slug := REPLACE(temp_slug, 'к', 'k');
        temp_slug := REPLACE(temp_slug, 'л', 'l');
        temp_slug := REPLACE(temp_slug, 'м', 'm');
        temp_slug := REPLACE(temp_slug, 'н', 'n');
        temp_slug := REPLACE(temp_slug, 'о', 'o');
        temp_slug := REPLACE(temp_slug, 'п', 'p');
        temp_slug := REPLACE(temp_slug, 'р', 'r');
        temp_slug := REPLACE(temp_slug, 'с', 's');
        temp_slug := REPLACE(temp_slug, 'т', 't');
        temp_slug := REPLACE(temp_slug, 'у', 'u');
        temp_slug := REPLACE(temp_slug, 'ф', 'f');
        temp_slug := REPLACE(temp_slug, 'х', 'kh');
        temp_slug := REPLACE(temp_slug, 'ц', 'ts');
        temp_slug := REPLACE(temp_slug, 'ч', 'ch');
        temp_slug := REPLACE(temp_slug, 'ш', 'sh');
        temp_slug := REPLACE(temp_slug, 'щ', 'shch');
        temp_slug := REPLACE(temp_slug, 'ъ', '');
        temp_slug := REPLACE(temp_slug, 'ы', 'y');
        temp_slug := REPLACE(temp_slug, 'ь', '');
        temp_slug := REPLACE(temp_slug, 'э', 'e');
        temp_slug := REPLACE(temp_slug, 'ю', 'yu');
        temp_slug := REPLACE(temp_slug, 'я', 'ya');
        temp_slug := REGEXP_REPLACE(REPLACE(temp_slug, ' ', '-'), '[^a-z0-9-]+', '-', 'g');

        NEW.slug := temp_slug || NEW.id;
    ELSE
        NEW.slug := 'default-' || NEW.id;
    END IF;

    RAISE NOTICE 'table: %, name: %, slug: %', TG_TABLE_NAME, NEW.name, NEW.slug;
    RETURN NEW;
END;
$$;
--rollback DROP FUNCTION set_slug_function();