CREATE TABLE if not exists project
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active      BOOLEAN   default true,
    created_at  timestamp default now()
);

CREATE TABLE IF NOT EXISTS catalog
(
    id          SERIAL PRIMARY KEY,
    project_id  INT references project (id),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active      BOOLEAN   default true,
    version     VARCHAR(255),
    created_at  timestamp default now()
);

CREATE TABLE event
(
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_id       INT references catalog (id),
    name             VARCHAR(255),
    parameters       jsonb,
    local_created_at timestamp,
    created_at       timestamp default now()
);