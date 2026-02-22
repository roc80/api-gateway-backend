import org.springframework.boot.gradle.tasks.bundling.BootJar

val jooqVersion by extra("3.19.24")
val testcontainersVersion by extra("2.0.2")

val dbUrl = "jdbc:postgresql://localhost:5432/api_gateway"
val dbUser = "postgres"
val dbPassword = "123456"
val dbDockerContainer = "api-gateway-postgres-dev"

plugins {
    java
    `java-library`
    jacoco
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("pmd")
    id("org.jooq.jooq-codegen-gradle") version "3.19.24"
    id("com.diffplug.spotless") version "7.1.0"
}

sourceSets {
    main {
        java {
            srcDir("src/generated/java")
        }
    }
}

group = "com.zl.mjga"
version = "1.0.0"
description = "make java great again!"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        resolutionStrategy {
            // 防止SpringBoot3.5.5的依赖传递覆盖自定义的版本
            force("org.testcontainers:testcontainers:$testcontainersVersion")
            force("org.testcontainers:testcontainers-core:$testcontainersVersion")
            force("org.testcontainers:jdbc:$testcontainersVersion")
            force("org.testcontainers:database-commons:$testcontainersVersion")
            force("org.testcontainers:testcontainers-database-commons:$testcontainersVersion")
            force("org.testcontainers:testcontainers-jdbc:$testcontainersVersion")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

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
    implementation("org.jooq:jooq-meta:$jooqVersion")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.roc:api_mock_starter:0.0.1")

    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
    testImplementation("org.springframework.boot:spring-boot-testcontainers") {
        // spring-boot-testcontainers 内部依赖了 org.testcontainers:testcontainers:1.21.3
        exclude(group = "org.testcontainers", module = "testcontainers")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")

    jooqCodegen("org.postgresql:postgresql")
    jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    api("org.jspecify:jspecify:1.0.0")
}

tasks.withType<BootJar> {
    archiveFileName.set("app.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
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

pmd {
    sourceSets = listOf(java.sourceSets.findByName("main"))
    isConsoleOutput = true
    toolVersion = "7.15.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = files("pmd-rules.xml")
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    java {
        // 只格式化源码目录，明确排除生成的代码
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        targetExclude("build/generated-sources/**")
        googleJavaFormat("1.28.0").reflowLongStrings().aosp()
        formatAnnotations()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl
            user = dbUser
            password = dbPassword
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
    input: File? = null,
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

                // 使用参数化方式构建 SQL (转义字符串)
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
