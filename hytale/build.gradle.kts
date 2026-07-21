import me.modmuss50.mpp.ReleaseType

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
}

base.archivesName = "${project["archives_base_name"]}-${name}-for-Hytale${project["hytale_version"]}"
version = mod_version

repositories {
    maven("https://maven.hytale.com/release") {
        name = "Hytale"
        content {
            includeGroup("com.hypixel.hytale")
        }
    }
}

val compileApi = configurations.register("compileApi").get()
configurations.api.configure { extendsFrom(compileApi) }

dependencies {
    // Hytale Server 0.5.7 ships as Java 25 bytecode (major 69), matching this project's java_version.
    compileOnly("com.hypixel.hytale:Server:${project["hytale_version"]}")

    // The Emotecraft engine itself: emote (de)serialization, the keyframe model and playback maths.
    compileApi(project(":emotesAssets"))
    compileApi(project(":emotesServer"))

    compileApi("org.redlance.emotecraftlibrary:game-sdk:${project["redlanceemotes_version"]}")

    // The Hytale server ships no SLF4J at all, so CommonData.LOGGER would blow up with NoClassDefFoundError.
    // Bundle the API together with the JDK binding, which lands on java.util.logging - the same sink Hytale's
    // Flogger writes to, so Emotecraft's log lines end up in the regular server log.
    compileApi("org.slf4j:slf4j-jdk14:${project["slf4j_version"]}")
}

tasks {
    processResources {
        // mod_description spans several lines, which would be raw newlines inside a JSON string literal - escape them
        // so the generated manifest.json stays parseable.
        val description = project["mod_description"].replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")

        inputs.property("version", version)
        inputs.property("description", description)

        filesMatching("manifest.json") {
            expand("version" to version, "description" to description)
        }
    }

    shadowJar {
        configurations = listOf(compileApi)
        archiveClassifier.set("")
        mergeServiceFiles()

        relocate("team.unnamed.mocha", "io.github.kosmx.emotes.hytale.libs.mocha")
    }

    jar {
        archiveClassifier.set("dev")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

java {
    withSourcesJar()
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesHytale"
            from(components["java"])
            withCustomPom("emotesHytale", "Hytale Emotecraft server mod")
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

publishMods {
    // Only GitHub is wired up: Modrinth/CurseForge have no Hytale loader to publish under yet.
    file.set(tasks.shadowJar.get().archiveFile)

    type = ReleaseType./*of(releaseType)*/ALPHA // Force alpha
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }
}
