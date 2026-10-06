CREATE TABLE IF NOT EXISTS api_gateway.api_interface
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    code        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    enabled     BOOLEAN             NOT NULL DEFAULT TRUE,
    category    VARCHAR(100),
    owner       VARCHAR(100),
    create_time TIMESTAMPTZ         NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ         NOT NULL DEFAULT now(),
    deleted     BOOLEAN             NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_api_interface_active
    ON api_gateway.api_interface (enabled) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS api_gateway.api_interface_version
(
    id               BIGSERIAL PRIMARY KEY,
    api_id           BIGINT       NOT NULL REFERENCES api_gateway.api_interface (id),
    version          VARCHAR(50)  NOT NULL,
    is_current       BOOLEAN      NOT NULL DEFAULT FALSE,
    http_method      VARCHAR(10)  NOT NULL,
    path             VARCHAR(255) NOT NULL,
    request_headers  JSONB        NOT NULL DEFAULT '{}'::jsonb,
    request_params   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    request_body     JSONB,
    response_body    JSONB,
    response_example JSONB,
    example_curl     TEXT,
    example_code     JSONB,
    auth_type        VARCHAR(50),
    allow_invoke     BOOLEAN      NOT NULL DEFAULT TRUE,
    create_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    update_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_api_version UNIQUE (api_id, version)
);

CREATE TABLE IF NOT EXISTS api_gateway.api_interface_call_log
(
    id            BIGSERIAL PRIMARY KEY,
    api_id        BIGINT      NOT NULL,
    version_id    BIGINT      NOT NULL,
    caller        VARCHAR(100),
    request_data  JSONB,
    response_data JSONB,
    status_code   INT,
    success       BOOLEAN,
    duration_ms   INT,
    create_time   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_api_interface_call_log_create_time
    ON api_gateway.api_interface_call_log (create_time);
