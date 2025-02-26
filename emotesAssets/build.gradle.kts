plugins {
    `maven-publish`
}

version = mod_version

tasks.withType<Copy> {
    eachFile {
        rename {
            it.lowercase()
        }
        //Rename every file to lowercase. This is essential for the translations to work
        //Possibly creates other problems on other operating systems
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