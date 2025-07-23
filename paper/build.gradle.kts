import me.modmuss50.mpp.ReleaseType

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
}

base.archivesName = "${archives_base_name}-${name}-for-MC${minecraft_version}"
version = mod_version

val compileApi = configurations.register("compileApi").get()
configurations.api.configure { extendsFrom(compileApi) }

dependencies {
    paperweight.paperDevBundle("${minecraft_version}-R0.1-SNAPSHOT")

    compileApi(project(":emotesServer")) {
        exclude(group = "org.jetbrains", module = "annotations")

        exclude(module = "gson")
        exclude(module = "slf4j-api")
        exclude(module = "fastutil")
        exclude(module = "netty-buffer")
        exclude(module = "jspecify")
        exclude(module = "guava")
        exclude(module = "error_prone_annotations")
        exclude(module = "netty-buffer")
    }
    compileApi(project(":emotesAssets"))
    compileApi(project(path = ":emotesMc", configuration = "namedElements")) { isTransitive = false }

    implementation("com.velocitypowered:velocity-native") {
        version {
            strictly("3.4.0-SNAPSHOT")
        }
    }
}

tasks.runServer {
    minecraftVersion(minecraft_version)
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("description", mod_description)
    inputs.property("mcversion", minecraft_version)

    filesMatching("paper-plugin.yml") {
        expand("version" to version, "description" to mod_description, "mcversion" to minecraft_version)
    }
}

tasks.shadowJar {
    configurations = listOf(compileApi)
    archiveClassifier.set("")
    mergeServiceFiles()

    relocate("team.unnamed.mocha", "com.zigythebird.playeranim.lib.mochafloats")
    relocate("javassist", "com.zigythebird.playeranim.lib.javassist")
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesBukkit"
            from(components["java"])
            withCustomPom("emotesBukkit", "Minecraft Emotecraft Paper plugin")
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
    modLoaders.add("paper")
    modLoaders.add("folia")
    file.set(tasks.shadowJar.get().archiveFile)
    type = ReleaseType.of(releaseType)
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        announcementTitle = "Modrinth (Paper)"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.add(minecraft_version)
        displayName = mod_version
        version = "${mod_version}+${removePreRc(minecraft_version)}-paper"
    }
}

tasks.getByName("publishMods").dependsOn("publishPluginPublicationToHangar")

hangarPublish.publications.register("plugin") {
    version = "${mod_version}+${minecraft_version}-paper"
    channel = when (releaseType) { // convert to set channel names
        "stable" -> "Release"
        "beta" -> "Beta"
        else -> "Alpha"
    }
    id = providers.gradleProperty("hangar_id")
    apiKey = providers.environmentVariable("HANGAR_TOKEN")
    changelog = changes
    platforms.register("PAPER") {
        jar = tasks.shadowJar.flatMap { it.archiveFile }
        platformVersions = listOf(minecraft_version)
    }
}

