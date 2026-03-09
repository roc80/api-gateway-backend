import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "API Gateway Mock API - 测试模拟接口模块"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.roc:api-client-sdk:0.0.1")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<BootJar> {
    archiveFileName.set("mockapi-app.jar")
}
