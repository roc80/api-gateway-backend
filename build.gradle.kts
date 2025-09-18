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
java.sourceCompatibility = JavaVersion.VERSION_17

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
        googleJavaFormat("1.28.0").reflowLongStrings()
        formatAnnotations()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

jooq {
    configuration {
        generator {
            database {
                includes = ".*"
                excludes = "qrtz_.*"
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/*.sql"
                    }
                    property {
                        key = "sort"
                        value = "semantic"
                    }
                    property {
                        key = "unqualifiedSchema"
                        value = "none"
                    }
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                    property {
                        key = "logExecutedQueries"
                        value = "true"
                    }
                    property {
                        key = "logExecutionResults"
                        value = "true"
                    }
                }
            }
            generate {
                isDaos = true
                isRecords = true
                isDeprecated = false
                isImmutablePojos = false
                isFluentSetters = true
                isSpringAnnotations = true
                isSpringDao = true
            }
            target {
                packageName = "org.jooq.generated"
                directory = "build/generated-sources/jooq"
            }
        }
    }
}
