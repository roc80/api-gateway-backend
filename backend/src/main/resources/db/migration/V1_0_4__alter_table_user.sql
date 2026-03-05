ALTER TABLE api_gateway.user
ADD COLUMN access_key VARCHAR(1024),
ADD COLUMN secret_key VARCHAR(1024);
COMMENT ON COLUMN api_gateway.user.access_key IS '用户访问凭证';
COMMENT ON COLUMN api_gateway.user.secret_key IS '用户访问密钥';