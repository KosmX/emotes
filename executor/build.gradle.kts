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
            artifactId = "emotesExecutor"

            from(components["java"])

            withCustomPom("executor", "Minecraft Emotecraft executor interface")
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