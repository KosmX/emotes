plugins {
    `maven-publish`
}

version = mod_version

tasks.processResources {
    filesMatching("assets/lang/*.json") {
        rename {
            // Rename every file to lowercase. This is essential for the translations to work
            // Possibly creates other problems on other operating systems
            it.lowercase()
        }
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesAssets"
            from(components["java"])
            withCustomPom("emotesAssets", "Minecraft Emotecraft Assets")
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