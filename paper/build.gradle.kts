import me.modmuss50.mpp.ReleaseType

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    `maven-publish`
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
}

base.archivesName = "${project["archives_base_name"]}-${name}-for-MC${project["minecraft_version"]}"
version = mod_version

val compileApi = configurations.register("compileApi").get()
configurations.api.configure { extendsFrom(compileApi) }

dependencies {
    paperweight.paperDevBundle("${project["minecraft_version"]}+")

    compileApi(project(":emotesAssets"))
    compileApi(project(":emotesMc"))

    compileApi("org.redlance.common-utils:reflect:${project["commonutils_version"]}") {
        isTransitive = false
    }
}

tasks.runServer {
    minecraftVersion(project["minecraft_version"])
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("description", project["mod_description"])
    inputs.property("mcversion", project["minecraft_version"])

    filesMatching("paper-plugin.yml") {
        expand("version" to version, "description" to project["mod_description"], "mcversion" to project["minecraft_version"])
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.WARN
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

shadow {
    addShadowVariantIntoJavaComponent = false
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
        minecraftVersions.addAll(release_minecraft_versions)
        displayName = "Emotecraft $mod_version for Paper"
        version = "$mod_version-paper"
        environment = SERVER_ONLY
    }
}

tasks.getByName("publishMods").dependsOn("publishPluginPublicationToHangar")

hangarPublish.publications.register("plugin") {
    version = mod_version
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
        platformVersions = release_minecraft_versions
    }
}
