// BootJar 需要显式导入
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.springdoc.openapi-gradle-plugin")
    id("org.jooq.jooq-codegen-gradle")
    id("jacoco")
}

description = "API Gateway Backend - 业务服务模块"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.4.0")
    implementation("org.jooq:jooq-meta:${rootProject.extra["jooqVersion"]}")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.roc:api-client-sdk:0.0.1")

    testImplementation("org.testcontainers:testcontainers:${rootProject.extra["testcontainersVersion"]}")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:${rootProject.extra["testcontainersVersion"]}")
    testImplementation("org.testcontainers:testcontainers-postgresql:${rootProject.extra["testcontainersVersion"]}")
    testImplementation("org.springframework.boot:spring-boot-testcontainers") {
        exclude(group = "org.testcontainers", module = "testcontainers")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")

    jooqCodegen("org.postgresql:postgresql")
    jooqCodegen("org.jooq:jooq-codegen:${rootProject.extra["jooqVersion"]}")
    jooqCodegen("org.jooq:jooq-meta-extensions:${rootProject.extra["jooqVersion"]}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    api("org.jspecify:jspecify:1.0.0")
}

// JOOQ 生成源码目录
sourceSets {
    main {
        java {
            srcDir("src/generated/java")
        }
    }
}

tasks.withType<BootJar> {
    archiveFileName.set("backend-app.jar")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.13"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

// JOOQ 配置
jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = rootProject.extra["dbUrl"] as String
            user = rootProject.extra["dbUser"] as String
            password = rootProject.extra["dbPassword"] as String
        }
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "api_gateway"
                includes = ".*"
                excludes = "qrtz_.*"
            }
            generate {
                isDaos = true
                isRecords = true
                isFluentSetters = true
                isSpringAnnotations = true
                isSpringDao = true
            }
            target {
                packageName = "org.jooq.generated.api_gateway"
                directory = "$projectDir/src/generated/java"
            }
        }
    }
}

fun executeCommand(
    vararg command: String,
    input: java.io.File? = null,
): String {
    val pb = ProcessBuilder(*command)
    input?.let { pb.redirectInput(ProcessBuilder.Redirect.from(it)) }
    val process = pb.start()
    try {
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().use { it.readText() }
            throw GradleException("Command failed: ${command.joinToString(" ")}\nError: $error")
        }
        return output
    } finally {
        process.destroy()
    }
}

fun escapeSqlString(str: String): String = str.replace("'", "''").replace("\\", "\\\\")

tasks.register("flywayMigrateDocker") {
    group = "database"
    description = "Execute Flyway migrations using Docker"

    doLast {
        val migrationDir = file("src/main/resources/db/migration")
        val dbUrl = rootProject.extra["dbUrl"] as String
        val dbUser = rootProject.extra["dbUser"] as String
        val dbPassword = rootProject.extra["dbPassword"] as String
        val dbDockerContainer = rootProject.extra["dbDockerContainer"] as String

        // 检查容器是否运行
        val psResult =
            executeCommand(
                "docker",
                "ps",
                "--filter",
                "name=$dbDockerContainer",
                "--format",
                "{{.Names}}",
            )
        if (psResult.trim() != dbDockerContainer) {
            throw GradleException("Docker container '$dbDockerContainer' is not running.")
        }

        // 初始化 schema 和 flyway 历史表
        val initSql =
            """
            CREATE SCHEMA IF NOT EXISTS api_gateway;
            CREATE TABLE IF NOT EXISTS api_gateway.flyway_schema_history (
                installed_rank INT NOT NULL,
                version VARCHAR(50),
                description VARCHAR(200) NOT NULL,
                type VARCHAR(20) NOT NULL,
                script VARCHAR(1000) NOT NULL,
                checksum INT,
                installed_by VARCHAR(100) NOT NULL,
                installed_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                execution_time INT NOT NULL,
                success BOOLEAN NOT NULL,
                CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
            );
            """.trimIndent()

        executeCommand(
            "docker",
            "exec",
            "-i",
            dbDockerContainer,
            "psql",
            "-U",
            dbUser,
            "-d",
            "api_gateway",
            "-c",
            initSql,
        )

        // 获取已执行的迁移
        val existingMigrationsResult =
            executeCommand(
                "docker",
                "exec",
                "-i",
                dbDockerContainer,
                "psql",
                "-U",
                dbUser,
                "-d",
                "api_gateway",
                "-t",
                "-c",
                "SELECT script FROM api_gateway.flyway_schema_history WHERE success = true;",
            )
        val existingMigrations =
            existingMigrationsResult
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()

        migrationDir
            .listFiles()
            ?.filter { it.extension == "sql" }
            ?.sortedBy { it.name }
            ?.forEach { file ->
                if (existingMigrations.contains(file.name)) {
                    println("Skipping: ${file.name}")
                    return@forEach
                }

                println("Migrating: ${file.name}")
                val startTime = System.currentTimeMillis()

                // 执行迁移文件
                executeCommand(
                    "docker",
                    "exec",
                    "-i",
                    dbDockerContainer,
                    "psql",
                    "-U",
                    dbUser,
                    "-d",
                    "api_gateway",
                    input = file,
                )

                val executionTime = (System.currentTimeMillis() - startTime).toInt()

                // 记录成功的迁移
                val version =
                    file.name
                        .substringAfter("V")
                        .substringBefore("__")
                        .replace("_", ".")
                val description =
                    file.name
                        .substringAfter("__")
                        .substringBefore(".sql")
                        .replace("_", " ")

                val escapedScriptName = escapeSqlString(file.name)
                val escapedVersion = escapeSqlString(version)
                val escapedDescription = escapeSqlString(description)

                val insertSql =
                    """
                    INSERT INTO api_gateway.flyway_schema_history
                    (installed_rank, version, description, type, script, installed_by, installed_on, execution_time, success)
                    VALUES ((SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM api_gateway.flyway_schema_history),
                        '$escapedVersion', '$escapedDescription', 'SQL', '$escapedScriptName', '$dbUser', CURRENT_TIMESTAMP, $executionTime, true)
                    """.trimIndent()

                executeCommand(
                    "docker",
                    "exec",
                    "-i",
                    dbDockerContainer,
                    "psql",
                    "-U",
                    dbUser,
                    "-d",
                    "api_gateway",
                    "-c",
                    insertSql,
                )
            }
    }
}

tasks.jooqCodegen {
    dependsOn("flywayMigrateDocker")
}
