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

/**
 * Geyser will always behind the Java version in the main project (unfortunately)
 */
val targetGeyserJava = JavaVersion.VERSION_17

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
        exclude(module = "fastutil")
        exclude(module = "netty-buffer")
        exclude(module = "jspecify")
        exclude(module = "guava")
        exclude(module = "error_prone_annotations")
        exclude(module = "netty-buffer")
    }

    // Fastutil
    compileApi("org.cloudburstmc.fastutil.maps:byte-byte-maps:8.5.15") {
        isTransitive = false
    }
    compileApi("org.cloudburstmc.fastutil.maps:byte-object-maps:8.5.15") {
        isTransitive = false
    }
    compileApi("org.cloudburstmc.fastutil.commons:float-common:8.5.15") {
        isTransitive = false
    }
    compileApi("org.redlance:common-utils-common:1.0.1") {
        isTransitive = false
    }
    compileApi("org.ow2.asm:asm:9.9.1")
    compileApi("org.ow2.asm:asm-commons:9.9.1")
}

tasks {
    processResources {
        val apiVersion = (project.properties["geyser_version"] as String).removeSuffix("-SNAPSHOT")

        inputs.property("version", version)
        inputs.property("description", mod_description)
        inputs.property("apiversion", apiVersion)

        filesMatching("extension.yml") {
            expand("version" to version, "description" to mod_description, "apiversion" to apiVersion)
        }
    }

    shadowJar {
        configurations = listOf(compileApi)
        archiveClassifier.set("shaded")
        mergeServiceFiles()

        relocate("javassist", "org.redlance.dima_dencep.mods.emotecraft.geyser.libs.javassist")
        relocate("org.objectweb", "org.redlance.dima_dencep.mods.emotecraft.geyser.libs.ow")
    }

    downgradeJar {
        dependsOn(shadowJar)

        val from = project.java_version.majorVersion.toInt()
        val to = targetGeyserJava.majorVersion.toInt()

        multiReleaseOriginal = true
        multiReleaseVersions = (from downTo to).map { JavaVersion.toVersion(it) }

        downgradeTo = targetGeyserJava
        classpath = configurations.compileClasspath.get()
        inputFile = shadowJar.get().archiveFile
    }

    shadeDowngradedApi {
        downgradeTo = targetGeyserJava
        shadePath.set({ "org/redlance/dima_dencep/mods/emotecraft/geyser/libs/" })
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("dev")
        manifest {
            attributes("Multi-Release" to true)
        }
    }

    assemble {
        dependsOn(shadeDowngradedApi)
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifact(tasks.shadeDowngradedApi) {
                classifier = "java${targetGeyserJava.majorVersion.toInt()}"
                extension = "jar"
            }
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

    file.set(tasks.shadeDowngradedApi.get().archiveFile) // Java 17
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
