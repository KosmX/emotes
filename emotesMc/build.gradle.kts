plugins {
    id("xyz.wagyourtail.unimined")
    `maven-publish`
}

unimined.minecraft {
    version(minecraft_version)

    mappings {
        devNamespace("official")
    }

    source {
        sourceGenerator.javaVersion = java_version
        sourceGenerator.generator("1.12.0")
    }

    runs {
        off = true
    }

    defaultRemapJar = false
}

dependencies {
    api(project(":emotesServer")) {
        exclude(group = "org.jetbrains", module = "annotations")

        exclude(module = "gson")
        exclude(module = "slf4j-api")
        exclude(module = "fastutil")
        exclude(module = "guava")
        exclude(module = "netty-buffer")
        exclude(module = "joml")
    }
}

java {
    withSourcesJar()
}

tasks.jar {
    archiveClassifier = ""
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
