plugins {
    `java-library`
    `maven-publish`
}

description = "The Dubbo service API definitions defined in the project."

group = "com.roc"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "api"
        }
    }
}