plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api(project(":emotesAPI"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesServer"

            from(components["java"])

            withCustomPom("emotesServer", "Minecraft Emotecraft server common module")
        }
    }

    repositories {
        if (shouldPublishMaven) {
            kosmxRepo(project)
        } else {
            mavenLocal()
        }
    }
}