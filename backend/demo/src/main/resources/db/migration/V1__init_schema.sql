CREATE TABLE app_user (
    id            VARCHAR(64)  PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL
);

CREATE TABLE service_request (
    id              VARCHAR(64)   PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    description     VARCHAR(4000) NOT NULL,
    category        VARCHAR(64)   NOT NULL,
    status          VARCHAR(32)   NOT NULL,
    submitted_by_id VARCHAR(64)   NOT NULL REFERENCES app_user (id),
    assigned_to_id  VARCHAR(64)   NULL REFERENCES app_user (id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE status_history (
    id            VARCHAR(64)  PRIMARY KEY,
    request_id    VARCHAR(64)  NOT NULL REFERENCES service_request (id),
    from_status   VARCHAR(32)  NULL,
    to_status     VARCHAR(32)  NOT NULL,
    changed_by_id VARCHAR(64)  NOT NULL REFERENCES app_user (id),
    changed_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    note          VARCHAR(1000) NULL
);

CREATE TABLE request_comment (
    id         VARCHAR(64)   PRIMARY KEY,
    request_id VARCHAR(64)   NOT NULL REFERENCES service_request (id),
    author_id  VARCHAR(64)   NOT NULL REFERENCES app_user (id),
    content    VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_service_request_submitted_by ON service_request (submitted_by_id);
CREATE INDEX idx_service_request_status ON service_request (status);
CREATE INDEX idx_status_history_request ON status_history (request_id);
CREATE INDEX idx_comment_request ON request_comment (request_id);
