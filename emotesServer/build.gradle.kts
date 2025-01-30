plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api(project(":executor"))
    implementation("com.google.code.gson:gson:2.11.0")
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