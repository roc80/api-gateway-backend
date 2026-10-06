# API Gateway Backend

<div style="text-align: center;">

[![wakatime](https://wakatime.com/badge/github/roc80/api-gateway-backend.svg)](https://wakatime.com/badge/github/roc80/api-gateway-backend)

</div>

---

## 注意事项

### 模块说明

- `api-contract`：API 签名协议与通用契约（网关验签、接口契约的权威定义）

### IDEA设置

1. IDEA Project SDK选择 build.gradle.kts中指定的JDK版本
2. IDEA Settings - Build, Execution, Deployment - Build Tools - Gradle - Build and run using IntelliJ IDEA


### 运行前置步骤

#### Nacos服务

```shell

docker pull nacos/nacos-server

```

```shell

docker run -d `
    --name nacos `
    -e MODE=standalone `
    -e NACOS_AUTH_ENABLE=false `
    -e NACOS_AUTH_TOKEN="SW52YWxpZFRva2VuQmFzZTY0U3RyaW5nV2l0aDMyQnl0ZXNMZW5ndGg=" `
    -e NACOS_AUTH_IDENTITY_KEY="serverIdentity" `
    -e NACOS_AUTH_IDENTITY_VALUE="security" `
    -p 8080:8080 `
    -p 8848:8848 `
    -p 9848:9848 `
    nacos/nacos-server:latest
```

#### DockerCompose服务

```shell

# 启动依赖服务
docker-compose -f compose-dev.yaml up -d
```

#### JOOQ代码生成

```shell

# 首次启动或改动sql后，手动执行，生成jooq模板代码
 .\gradlew.bat jooqCodegen
```

### 其他

#### 代码风格检查

```shell

# build时如果spotlessCheck失败，手动执行
.\gradlew.bat spotlessApply
```

## TODO
- [x] 代码遗留todo处理（网关验签所需的 sk 由硬编码改为经 Dubbo RPC 按 ak 实时查询，并校验用户启用状态；"接口是否存在"与 invoke 端点参数化依赖接口注册/上传流程，归入第4项；接口调用次数统计归入第5项）
- [x] 将RPC接口定义抽离到独立的Gradle module（`api` 模块，backend 为 provider、gateway 为 consumer）
- [x] 检查接口权限，为一些接口设置管理员权限调用。提供用户申请成为管理员的机制。（接口/版本管理的写操作需 `WRITE_INTERFACE` 权限（仅 ADMIN 持有，迁移 V1_0_5）；申请流：`POST /urp/me/apply-admin` 绑定 PENDING_ADMIN 标记角色（迁移 V1_0_6），管理员经 `GET /urp/pending-admins`、`POST /urp/pending-admins/approve|reject` 审批）
- [x] 提供用户上传API的功能：用户的接口检查、审核等等。需要符合特定规则。（`POST /interfaces/upload` 上传并校验规则（code 唯一、方法+路径不与已注册当前版本冲突、版本号/路径格式约束），初始 `enabled=false` 待审核；管理员用 `PATCH /interfaces/{id}/enabled` 审核启用、`enabled=false` 过滤查询待审列表；网关转发前经 Dubbo 校验接口已注册且可调用（未注册 404，迁移 V1_0_7 为存量 /api/name 补注册数据）；invoke 端点改为 `POST /interfaces/invoke/{apiId}` 按当前版本解析 method/path 并记录真实 apiId/versionId）
- [x] 提供统计分析功能，统计接口调用情况、用户调用次数情况，方便前端可视化展示图表（含网关侧接口调用次数统计埋点）（网关转发结束经 Dubbo `InterfaceCallReportService#reportCall` 异步埋点落库，真实记录 apiId/versionId/caller/状态码/成功标记/耗时，替代 invoke 端点重复写日志避免双计数；统计查询 `GET /interfaces/stats/overview|trend|top-interfaces|top-users`，支持时间范围与 day/hour 粒度、趋势空档补零对齐 UTC，迁移 V1_0_8 为 create_time 建索引）

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=roc80/api-gateway-backend&type=Date)](https://www.star-history.com/#roc80/api-gateway-backend&Date)
