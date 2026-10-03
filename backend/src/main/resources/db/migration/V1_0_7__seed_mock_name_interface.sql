-- 网关启用"接口是否存在"校验后，存量 mock 接口 /api/name 需要注册数据才能继续调用
INSERT INTO "api_gateway"."api_interface"
    ("name", "code", "description", "enabled", "category", "owner")
VALUES ('模拟用户名接口', 'mock-name-api', 'mockapi 提供的示例接口：POST /api/name 返回用户名', TRUE, 'mock', 'platform');

INSERT INTO "api_gateway"."api_interface_version"
    ("api_id", "version", "is_current", "http_method", "path", "allow_invoke")
SELECT "api_interface"."id", 'v1', TRUE, 'POST', '/api/name', TRUE
FROM "api_gateway"."api_interface"
WHERE "api_interface"."code" = 'mock-name-api';
