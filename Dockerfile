FROM bellsoft/liberica-openjdk-alpine:17 AS builder

ENV GRADLE_USER_HOME=/cache
WORKDIR /workspace

# 安装 bash 因为 gradlew 默认用 bash 或 sh，但 Alpine 的 sh 不兼容某些脚本
RUN apk add --no-cache bash dos2unix

# 复制全部项目
COPY . .

# 修复 gradlew 为 Linux 格式
RUN dos2unix gradlew
RUN chmod +x gradlew

# 构建（含 jOOQ 和 bootJar）
RUN --mount=type=cache,target=/cache \
    ./gradlew jooqCodegen \
    && ./gradlew bootJar --stacktrace

# ---------------------------------------------------
# Runtime 镜像（更小体积）
# ---------------------------------------------------
FROM bellsoft/liberica-openjdk-alpine:17 AS runner
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "/app/app.jar"]
