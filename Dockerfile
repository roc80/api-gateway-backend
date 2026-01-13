# ---------- Builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder

ENV GRADLE_USER_HOME=/cache
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"

WORKDIR /workspace

RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*

COPY . .

RUN dos2unix gradlew && chmod +x gradlew

RUN --mount=type=cache,target=/cache \
    --mount=type=cache,target=/root/.gradle \
    RUN ./gradlew bootJar --no-daemon --stacktrace

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "/app/app.jar"]
