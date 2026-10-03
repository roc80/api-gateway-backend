-- 管理员申请机制：PENDING_ADMIN 为申请标记角色
-- 用户申请时绑定该角色，审批通过后置换为 ADMIN，驳回时移除
INSERT INTO "api_gateway"."role" ("code", "name")
VALUES ('PENDING_ADMIN', 'PENDING_ADMIN');
