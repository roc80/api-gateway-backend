# API Gateway Backend

<div style="text-align: center;">

[![wakatime](https://wakatime.com/badge/github/roc80/api-gateway-backend.svg)](https://wakatime.com/badge/github/roc80/api-gateway-backend)

</div>

---

## 注意事项
- IDEA Project SDK, Gradle Jvm 选择 build.gradle.kts中指定的JDK版本
- 编译项目前，确保有docker环境，启动依赖的服务
```shell

# 启动依赖服务
docker-compose -f compose-dev.yaml up -d
```
- 首次启动或改动sql后，手动执行
```shell

# 生成jooq模板代码
 .\gradlew.bat jooqCodegen
```
- build时如果spotlessCheck失败，手动执行
```shell

# 应用spotless插件
.\gradlew.bat spotlessApply
```

## 开发计划

- [ ] ~~接口信息管理 12.21-12.22~~
- [x] 接口数据表设计 12.21-12.25
- [ ] 接口表CRUD 12.26-12.30


---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=roc80/api-gateway-backend&type=Date)](https://www.star-history.com/#roc80/api-gateway-backend&Date)
