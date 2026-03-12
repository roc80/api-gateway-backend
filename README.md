# API Gateway Backend

<div style="text-align: center;">

[![wakatime](https://wakatime.com/badge/github/roc80/api-gateway-backend.svg)](https://wakatime.com/badge/github/roc80/api-gateway-backend)

</div>

---

## 注意事项

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
- [ ] 代码遗留todo处理
- [ ] 将RPC接口定义抽离到独立的Gradle module
- [ ] 检查接口权限，为一些接口设置管理员权限调用。提供用户申请成为管理员的机制。
- [ ] 提供用户上传API的功能：用户的接口检查、审核等等。需要符合特定规则。
- [ ] 完善API Client SDK，将其命名为spring-boot-starter，优化结构，使其通用。
- [ ] 提供统计分析功能，统计接口调用情况、用户调用次数情况，方便前端可视化展示图表

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=roc80/api-gateway-backend&type=Date)](https://www.star-history.com/#roc80/api-gateway-backend&Date)
