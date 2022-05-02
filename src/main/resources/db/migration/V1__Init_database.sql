create table requests (
    id CHAR(26) PRIMARY KEY,
    status INTEGER NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);