import org.springframework.boot.gradle.tasks.bundling.BootJar

// 统一版本管理
val jooqVersion by extra("3.19.24")
val testcontainersVersion by extra("2.0.2")
val springCloudVersion by extra("2024.0.0")

// 数据库配置 (仅 backend 模块使用)
val dbUrl by extra("jdbc:postgresql://localhost:5432/api_gateway")
val dbUser by extra("postgres")
val dbPassword by extra("123456")
val dbDockerContainer by extra("api-gateway-postgres-dev")

plugins {
    java
    `java-library`
    jacoco
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0" apply false
    id("pmd")
    id("org.jooq.jooq-codegen-gradle") version "3.19.24" apply false
    id("com.diffplug.spotless") version "7.1.0"
}

allprojects {
    group = "com.zl.mjga"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

// 子项目通用配置
subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    spotless {
        format("misc") {
            target("*.gradle.kts", "*.md", ".gitignore")
            trimTrailingWhitespace()
            leadingTabsToSpaces()
            endWithNewline()
        }

        java {
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

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// 根项目不需要 BootJar
tasks.withType<BootJar> {
    enabled = false
}

// PMD 配置 (仅对 backend 模块生效)
pmd {
    toolVersion = "7.15.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = files("pmd-rules.xml")
}

// ========== 并行启动所有服务 ==========
tasks.register("startAll") {
    group = "application"
    description = "并行启动 backend (8080) 和 gateway (9090) 服务"

    dependsOn(":backend:bootRun", ":gateway:bootRun")
}
