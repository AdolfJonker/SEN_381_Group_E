-- CivicConnect initial schema (Entity Model + ADR-DATA-001)

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('Requester', 'Staff', 'Management'))
);

CREATE TABLE service_requests (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    description       TEXT NOT NULL,
    category          VARCHAR(64)  NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    submitted_by_id   BIGINT NOT NULL REFERENCES users (id),
    assigned_to_id    BIGINT NULL REFERENCES users (id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version           INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_request_status CHECK (
        status IN ('Open', 'Assigned', 'InProgress', 'Resolved', 'Closed')
    ),
    CONSTRAINT chk_request_category CHECK (
        category IN (
            'Facility Fault',
            'Damaged Equipment',
            'Security Concern',
            'IT Support',
            'Maintenance',
            'Lost Property',
            'Other'
        )
    )
);

CREATE INDEX idx_service_requests_submitted_by ON service_requests (submitted_by_id);
CREATE INDEX idx_service_requests_assigned_to ON service_requests (assigned_to_id);
CREATE INDEX idx_service_requests_status ON service_requests (status);
CREATE INDEX idx_service_requests_category ON service_requests (category);

CREATE TABLE status_history (
    id              BIGSERIAL PRIMARY KEY,
    request_id      BIGINT NOT NULL REFERENCES service_requests (id),
    from_status     VARCHAR(32) NOT NULL,
    to_status       VARCHAR(32) NOT NULL,
    changed_by_id   BIGINT NOT NULL REFERENCES users (id),
    "timestamp"      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    note            TEXT NULL
);

CREATE INDEX idx_status_history_request ON status_history (request_id);

CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    request_id      BIGINT NOT NULL REFERENCES service_requests (id),
    author_id       BIGINT NOT NULL REFERENCES users (id),
    content         TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_request ON comments (request_id);
