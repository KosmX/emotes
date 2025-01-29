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
//project.version = project.mod_version
version = project.mod_version


repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots") {
        name = "BucketMaven"
    }
}

val compileModule = configurations.register("compileModule").get()
configurations.implementation.configure { extendsFrom(compileModule) }

val compileApi = configurations.register("compileApi").get()
configurations.api.configure { extendsFrom(compileApi) }

dependencies {
    paperweight.paperDevBundle("${rootProject.minecraft_version}-R0.1-SNAPSHOT")

    compileApi(project(":emotesServer")) {
        isTransitive = true
        exclude(group = "org.jetbrains", module = "annotations")
    }
    compileApi(project(":emotesAssets"))
    compileApi(project(path = ":emotesMc", configuration = "namedElements")) { isTransitive = false }
}

tasks.runServer {
    minecraftVersion(rootProject.minecraft_version)
}

tasks.processResources {

    inputs.property("version", project.version)
    inputs.property("description", rootProject.mod_description)

    filesMatching("paper-plugin.yml") {
        expand("version" to project.version, "description" to rootProject.mod_description)
    }
}

tasks.shadowJar {
    configurations = listOf(compileModule, compileApi)
    archiveClassifier.set("")

    dependencies {
        exclude {
            return@exclude it.moduleGroup.startsWith("com.google")
        }
    }
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
            // add all the jars that should be included when publishing to maven

            artifactId = "emotesBukkit"

            // jar only with classes from this module, dependencies will be included in pom
            artifact(tasks.jar) {
                classifier = ""
            }
            artifact(tasks.sourcesJar)
            addDeps(project, compileApi, "compile")
            addDeps(project, configurations.implementation.get(), "runtime")
            withCustomPom("emotesBukkit", "Minecraft Emotecraft Paper plugin")
        }
    }

    repositories {
        if (project.shouldPublishMaven) {
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
        version = "${project.mod_version}+${project.minecraft_version}-paper"
    }
}
