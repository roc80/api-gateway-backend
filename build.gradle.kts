import org.springframework.boot.gradle.tasks.bundling.BootJar

val jooqVersion by extra("3.19.24")
val testcontainersVersion by extra("1.21.3")
val flywayVersion by extra("11.10.2")

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
            srcDir("build/generated-sources/jooq")
        }
    }
    test {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
}

group = "com.zl.mjga"
version = "1.0.0"
description = "make java great again!"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
// 不指定版本的话，compileJava 会失败
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
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
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.4.0")
    implementation("org.jooq:jooq-meta:$jooqVersion")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.1")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
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

// 修复任务依赖问题 - 使用字符串避免任务解析顺序问题
tasks.compileJava {
    dependsOn("jooqCodegen")
}

tasks.compileTestJava {
    dependsOn("jooqCodegen")
}

// 修复 Spotless 任务依赖 - 使用正确的任务名称和字符串方式
tasks.named("spotlessApply") {
    dependsOn("jooqCodegen")
}

tasks.named("spotlessCheck") {
    dependsOn("jooqCodegen")
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
            url = "jdbc:postgresql://localhost:5432/api_gateway"
            user = "postgres"
            password = "123456"
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
                directory = "build/generated-sources/jooq"
            }
        }
    }
}

// 通过 Docker 执行 Flyway 迁移
tasks.register("flywayMigrateDocker") {
    group = "database"
    description = "Execute Flyway migrations using Docker"

    doLast {
        val migrationDir = file("src/main/resources/db/migration")
        val containerName = "api-gateway-postgres-dev"

        // 检查容器是否运行
        val psProcess = ProcessBuilder("docker", "ps", "--filter", "name=$containerName", "--format", "{{.Names}}").start()
        if (psProcess.inputStream.bufferedReader().use { it.readText().trim() } != containerName) {
            throw GradleException("Docker container '$containerName' is not running.")
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

        ProcessBuilder(
            "docker",
            "exec",
            "-i",
            containerName,
            "psql",
            "-U",
            "postgres",
            "-d",
            "api_gateway",
            "-c",
            initSql,
        ).start().waitFor()

        // 获取已执行的迁移
        val existingMigrations = mutableSetOf<String>()
        ProcessBuilder(
            "docker",
            "exec",
            "-i",
            containerName,
            "psql",
            "-U",
            "postgres",
            "-d",
            "api_gateway",
            "-t",
            "-c",
            "SELECT script FROM api_gateway.flyway_schema_history WHERE success = true;",
        ).start()
            .inputStream
            .bufferedReader()
            .use { reader ->
                reader
                    .readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { existingMigrations.add(it) }
            }

        // 执行迁移文件
        migrationDir.listFiles()?.filter { it.extension == "sql" }?.sortedBy { it.name }?.forEach { file ->
            if (existingMigrations.contains(file.name)) {
                println("Skipping: ${file.name}")
                return@forEach
            }

            println("Migrating: ${file.name}")
            val startTime = System.currentTimeMillis()

            ProcessBuilder("docker", "exec", "-i", containerName, "psql", "-U", "postgres", "-d", "api_gateway")
                .redirectInput(ProcessBuilder.Redirect.from(file))
                .redirectErrorStream(true)
                .start()
                .let { process ->
                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    val executionTime = (System.currentTimeMillis() - startTime).toInt()

                    if (process.waitFor() != 0) {
                        throw GradleException("Migration failed: ${file.name}\n$output")
                    }

                    // 记录成功的迁移 (V1_0_0__desc.sql -> version=1.0.0, description=desc)
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

                    ProcessBuilder(
                        "docker",
                        "exec",
                        "-i",
                        containerName,
                        "psql",
                        "-U",
                        "postgres",
                        "-d",
                        "api_gateway",
                        "-c",
                        """
                        INSERT INTO api_gateway.flyway_schema_history
                        (installed_rank, version, description, type, script, installed_by, installed_on, execution_time, success)
                        VALUES ((SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM api_gateway.flyway_schema_history),
                                '$version', '$description', 'SQL', '${file.name}', 'postgres', CURRENT_TIMESTAMP, $executionTime, true)
                        """.trimIndent(),
                    ).start().waitFor()
                }
        }
    }
}

// 确保 Docker 迁移在 jOOQ 代码生成之前执行
tasks.jooqCodegen {
    dependsOn("flywayMigrateDocker")
}
