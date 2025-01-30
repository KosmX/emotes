import me.modmuss50.mpp.ReleaseType

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.12"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
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
    }
    compileApi(project(":emotesAssets"))
    compileApi(project(path = ":emotesMc", configuration = "namedElements")) { isTransitive = false }
}

tasks.runServer {
    minecraftVersion(minecraft_version)
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("description", mod_description)

    filesMatching("paper-plugin.yml") {
        expand("version" to version, "description" to mod_description)
    }
}

tasks.shadowJar {
    configurations = listOf(compileApi)
    archiveClassifier.set("")
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
    type = ReleaseType.of(if (releaseType == "release") "stable" else releaseType)
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.add(minecraft_version)
        displayName = mod_version
        version = "${mod_version}+${minecraft_version}-paper"
    }
}
