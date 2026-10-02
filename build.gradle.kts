plugins {
    java
    `java-library`
    jacoco
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "7.1.0"
}

allprojects {
    group = "com.zl.mjga"
    version = "1.0.0"

    repositories {
        // 不使用 mavenLocal：本仓库所有依赖均来自 Maven Central 或 project() 源码依赖，
        // 避免机器本地构件导致构建不可重现
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "jacoco")

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

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
    }
}
