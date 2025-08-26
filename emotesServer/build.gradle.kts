plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api(project(":emotesAPI"))
}

java {
    withSourcesJar()
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