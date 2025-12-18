import me.modmuss50.mpp.ReleaseType

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":minecraft:archCommon").loom.accessWidenerPath
}

val common = configurations.register("common").get()
val commonModule = configurations.register("commonModule").get()
val shadowCommon = configurations.register("shadowCommon").get()
val pomCompile = configurations.register("pomDep").get()


configurations.apply {
    common.extendsFrom(commonModule)
    shadowCommon.extendsFrom(commonModule)
    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    named("developmentFabric").configure { extendsFrom(common) }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")

    // Fabric API
    modImplementation(fabricApi.module("fabric-command-api-v2", project["fabric_api_version"]))
    modImplementation(fabricApi.module("fabric-networking-api-v1", project["fabric_api_version"]))
    modImplementation(fabricApi.module("fabric-key-binding-api-v1", project["fabric_api_version"]))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", project["fabric_api_version"]))
    modImplementation(fabricApi.module("fabric-rendering-v1", project["fabric_api_version"]))

    commonModule(project(":emotesAPI")) { isTransitive = false }
    commonModule(project(":emotesServer")) { isTransitive = false }
    commonModule(project(":emotesAssets")) { isTransitive = false }
    commonModule(project(path = ":emotesMc", configuration = "namedElements")) { isTransitive = false }

    modRuntimeOnly(fabricApi.module("fabric-screen-api-v1", project["fabric_api_version"]))
    modRuntimeOnly(fabricApi.module("fabric-resource-loader-v0", project["fabric_api_version"]))
    modImplementation("com.terraformersmc:modmenu:${project["modmenu_version"]}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modImplementation("me.lucko:fabric-permissions-api:${project["fabric_permissions_api"]}") {
        pomCompile(this)
    }

    modImplementation("com.zigythebird.playeranim:PlayerAnimationLibFabric:${project["playeranimlib_version"]}") {
        pomCompile(this)
    }

    /*modRuntimeOnly("com.zigythebird.bendable_cuboids:BendableCuboidsFabric:${project["bendablecuboids_version"]}") {
        isTransitive = false
        pomCompile(this)
    }*/

    implementation("net.raphimc:NoteBlockLib:${project["noteblocklib_version"]}") {
        include(this)
        pomCompile(this)
    }

    modRuntimeOnly("org.redlance.dima_dencep.mods:TranslationFallbacksFabric:${project["translationfallbacks_version"]}") {
        include(this)
        pomCompile(this)
    }

    // Third-party
    /*modImplementation("com.blamejared.searchables:Searchables-fabric-${minecraft_version}:${project["searchables_version"]}") {
        isTransitive = false
    }*/

    pomCompile(project(":emotesAssets"))
    pomCompile(project(":minecraft:archCommon"))

    common(project(path = ":minecraft:archCommon", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":minecraft:archCommon", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
}


tasks.processResources {
    inputs.property("version", version)
    inputs.property("description", mod_description)

    filesMatching("fabric.mod.json") {
        expand("version" to version, "description" to mod_description)
    }
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
// if it is present.
// If you remove this task, sources will not be generated.

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.WARN
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
    mergeServiceFiles()
}

tasks.remapJar {
    injectAccessWidener = true
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("dev")
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            // add all the jars that should be included when publishing to maven

            artifactId = "emotesFabric"

            artifact(tasks.remapJar) {
                builtBy(tasks.remapJar)
                classifier = ""
            }

            artifact(tasks.sourcesJar)

            addDeps(project, pomCompile, "compile")
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
    modLoaders.add("fabric")
    // modLoaders.add("quilt")
    file.set(tasks.remapJar.get().archiveFile)
    type = ReleaseType.of(releaseType)
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        announcementTitle = "Modrinth (Fabric)"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.addAll(release_minecraft_versions)
        displayName = mod_version
        version = "${mod_version}+${removePreRc(minecraft_version)}-fabric"

        requires("fabric-api")
        requires("player-animation-library")
        optional("bendable-cuboids")
        optional("searchables")
        optional("fabric-permissions-api")
    }

    curseforge {
        announcementTitle = "CurseForge (Fabric)"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id_fabric")
        projectSlug = providers.gradleProperty("curseforge_slug_fabric")
        changelogType = "markdown"
        displayName = base.archivesName.get() + "-$mod_version"
        minecraftVersions.addAll(curseforge_minecraft_versions)

        javaVersions.add(project.java_version)
        clientRequired = true
        serverRequired = true

        requires("fabric-api")
        requires("player-animation-library")
        optional("bendable-cuboids")
        optional("searchables")
    }
}
