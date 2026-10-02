plugins {
    `java-library`
    `maven-publish`
}

description = "API 签名协议与通用契约定义（api-client-sdk 对应的服务端权威实现）"

group = "com.roc"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "api-contract"
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
