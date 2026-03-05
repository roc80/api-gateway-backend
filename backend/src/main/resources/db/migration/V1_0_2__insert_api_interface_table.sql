CREATE TABLE IF NOT EXISTS api_gateway.api_interface
(
    id          BIGSERIAL PRIMARY KEY,

    -- 基础信息
    name        VARCHAR(100)        NOT NULL,
    code        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,

    -- 状态控制
    enabled     BOOLEAN             NOT NULL DEFAULT TRUE,

    -- 分类 & 归属
    category    VARCHAR(100),
    owner       VARCHAR(100),

    -- 审计字段（通用）
    create_time TIMESTAMPTZ         NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ         NOT NULL DEFAULT now(),
    deleted     BOOLEAN             NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_api_interface_active
    ON api_gateway.api_interface (enabled) WHERE deleted = FALSE;


COMMENT
    ON TABLE api_gateway.api_interface IS '接口定义主表';
COMMENT
    ON COLUMN api_gateway.api_interface.code IS '接口唯一标识';
COMMENT
    ON COLUMN api_gateway.api_interface.enabled IS '是否启用';

---

CREATE TABLE IF NOT EXISTS api_gateway.api_interface_version
(
    id               BIGSERIAL PRIMARY KEY,
    api_id           BIGINT       NOT NULL REFERENCES api_gateway.api_interface (id),

    -- 版本信息
    version          VARCHAR(50)  NOT NULL,
    is_current       BOOLEAN      NOT NULL DEFAULT FALSE,

    -- 请求定义
    http_method      VARCHAR(10)  NOT NULL,
    path             VARCHAR(255) NOT NULL,

    -- 请求结构
    request_headers  JSONB        NOT NULL DEFAULT '{}'::jsonb,
    request_params   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    request_body     JSONB,

    -- 响应定义
    response_body    JSONB,
    response_example JSONB,

    -- 调用示例
    example_curl     TEXT,
    example_code     JSONB,       -- { "java": "...", "js": "...", "python": "..." }

    -- 安全 & 调用控制
    auth_type        VARCHAR(50), -- NONE / API_KEY / OAUTH2
    allow_invoke     BOOLEAN      NOT NULL DEFAULT TRUE,

    -- 审计字段
    create_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    update_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_api_version UNIQUE (api_id, version)
);

COMMENT
    ON TABLE api_gateway.api_interface_version IS '接口版本定义';
COMMENT
    ON COLUMN api_gateway.api_interface_version.request_params IS 'Query/Path 参数定义';
COMMENT
    ON COLUMN api_gateway.api_interface_version.example_code IS '多语言示例代码';


---


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

COMMENT ON TABLE api_gateway.api_interface_call_log IS '接口在线调用日志';
