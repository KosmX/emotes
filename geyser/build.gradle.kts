import me.modmuss50.mpp.ReleaseType

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow")
    id("xyz.wagyourtail.jvmdowngrader")
    id("me.modmuss50.mod-publish-plugin")
}

base.archivesName = "${archives_base_name}-${name}-for-MC${minecraft_version}"
version = mod_version

val compileApi = configurations.register("compileApi").get()
configurations.api.configure { extendsFrom(compileApi) }

dependencies {
    compileOnly("org.geysermc.geyser:core:${properties["geyser_version"] as String}")
    implementation("org.geysermc.geyser:standalone:${properties["geyser_version"] as String}")

    compileApi(project(":emotesAssets"))
    compileApi(project(":emotesServer")) {
        exclude(group = "org.jetbrains", module = "annotations")

        exclude(module = "gson")
        exclude(module = "slf4j-api")
        // exclude(module = "fastutil")
        exclude(module = "netty-buffer")
        exclude(module = "jspecify")
        exclude(module = "guava")
        exclude(module = "error_prone_annotations")
        exclude(module = "netty-buffer")
    }
}

tasks {
    processResources {
        inputs.property("version", version)
        inputs.property("description", mod_description)

        filesMatching("extension.yml") {
            expand("version" to version, "description" to mod_description)
        }
    }

    shadowJar {
        configurations = listOf(compileApi)
        archiveClassifier.set("shaded")
        mergeServiceFiles()

        relocate("team.unnamed.mocha", "com.zigythebird.playeranim.lib.mochafloats")
        relocate("javassist", "com.zigythebird.playeranim.lib.javassist")
    }

    downgradeJar {
        dependsOn(shadowJar)

        downgradeTo = JavaVersion.VERSION_17
        inputFile = shadowJar.get().archiveFile
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("dev")
    }

    assemble {
        dependsOn(downgradeJar)
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesGeyser"
            from(components["java"])
            withCustomPom("emotesGeyser", "Minecraft Emotecraft Geyser extension")
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
    modLoaders.add("geyser")

    file.set(tasks.downgradeJar.get().archiveFile) // Java 17
    // additionalFiles.from(tasks.shadowJar.get().archiveFile) // Java 21

    type = ReleaseType./*of(releaseType)*/ALPHA // Force alpha
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        announcementTitle = "Modrinth (Geyser)"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.addAll(release_minecraft_versions)
        displayName = mod_version
        version = "${mod_version}+${removePreRc(minecraft_version)}-geyser"
    }
}
