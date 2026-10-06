-- 统计分析按时间范围聚合调用日志（总览/趋势/排行），为 create_time 范围扫描建索引
CREATE INDEX IF NOT EXISTS idx_api_interface_call_log_create_time
    ON api_gateway.api_interface_call_log (create_time);
