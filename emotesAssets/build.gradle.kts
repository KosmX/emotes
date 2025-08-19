plugins {
    `maven-publish`
}

version = mod_version

// Rename every file to lowercase. This is essential for the translations to work
// Possibly creates other problems on other operating systems
tasks.processResources {
    filesMatching("assets/emotecraft/lang/*.json") {
        val segments = relativePath.segments
        val newSegments = segments.dropLast(1) + segments.last().lowercase()
        relativePath = RelativePath(true, *newSegments.toTypedArray())
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