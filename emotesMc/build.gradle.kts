@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.architectury.loom")
    `maven-publish`
}

loom {
    silentMojangMappingsLicense()
}

version = mod_version

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.10:${parchment_version}@zip")
    })

    api(project(":emotesServer")) {
        exclude(module = "gson")
    }
}

java {
    withSourcesJar()
}

tasks.jar {
    archiveClassifier = ""
}

tasks.remapJar {
    enabled = false
}

tasks.remapSourcesJar {
    enabled = false
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesMc"

            artifact(tasks.jar)
            artifact(tasks.sourcesJar)

            addDeps(project, configurations.api, "compile")

            withCustomPom("emotesMc", "Emotecraft common serverside Minecraft code")
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
