-- 接口管理写权限：仅授予 ADMIN 角色（接口的创建/更新/启停/删除、版本管理需要该权限）
INSERT INTO "api_gateway"."permission" ("code", "name")
VALUES ('WRITE_INTERFACE', 'WRITE_INTERFACE');

INSERT INTO "api_gateway"."role_permission_map" ("role_id", "permission_id")
SELECT "role"."id", "permission"."id"
FROM "api_gateway"."role",
     "api_gateway"."permission"
WHERE "role"."code" = 'ADMIN'
  AND "permission"."code" = 'WRITE_INTERFACE';
