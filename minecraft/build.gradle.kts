import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.ReleaseType

plugins {
    id("xyz.wagyourtail.unimined")
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
    id("org.redlance.dima_dencep.gradle.machete") version "1.0.2"
}

sourceSets {
    main { // Fix resources
        resources.srcDir(project(":emotesAssets").file("src/main/resources"))
    }
    create("fabric")
    create("neoforge")
}

unimined.minecraft(sourceSets.main.get()) {
    version(minecraft_version)

    fabric {
        loader(fabric_loader_version)
        accessWidener(file("src/main/resources/emotes.classtweaker"))
    }

    source {
        sourceGenerator.javaVersion = java_version
        sourceGenerator.generator("1.12.0")
    }

    defaultRemapJar = false
}

unimined.minecraft(sourceSets.getByName("fabric")) {
    combineWith(sourceSets.main.get())

    fabric {
        loader(fabric_loader_version)
        accessWidener(file("src/main/resources/emotes.classtweaker"))
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.getByName("neoforge")) {
    combineWith(sourceSets.main.get())

    neoForge {
        loader("net.neoforged:neoforge:$neoforge_version:universal")
        mixinConfig("emotecraft-arch.mixins.json", "emotecraft-neo.mixins.json")

        accessTransformer(provider {
            val transformed = file("src/main/resources/emotes.classtweaker").readText()
                .replaceFirst(Regex("""classTweaker\s+v\d+\s+(\S+)"""), "accessWidener v2 $1")
                .lines()
                .filter { line ->
                    !line.trim().startsWith("transitive-inject-interface") && !line.trim().startsWith("inject-interface")
                }
                .joinToString("\n")

            val out = layout.buildDirectory.file("generated/emotes.classtweaker").get().asFile
            try {
                out.parentFile.mkdirs()
                out.writeText(transformed)
                aw2at(out)
            } finally {
                out.delete()
            }
        }.get())
    }
    defaultRemapJar = true
}

val commonModule = configurations.register("commonModule").get()
configurations.getByName("fabricRuntimeClasspath").extendsFrom(commonModule)
configurations.getByName("fabricCompileClasspath").extendsFrom(commonModule)
configurations.getByName("neoforgeRuntimeClasspath").extendsFrom(commonModule)
configurations.getByName("neoforgeCompileClasspath").extendsFrom(commonModule)
commonModule.isTransitive = false

val platformInclude = configurations.register("platformInclude").get()
configurations.getByName("fabricInclude").extendsFrom(platformInclude)
configurations.getByName("neoforgeInclude").extendsFrom(platformInclude)

val fabricPomCompile = configurations.register("fabricPomDep").get()
fabricPomCompile.extendsFrom(commonModule)

val neoforgePomCompile = configurations.register("neoPomDep").get()
neoforgePomCompile.extendsFrom(commonModule)

dependencies {
    // Common
    implementation(project(":emotesAssets")) { commonModule(this) }
    implementation(project(":emotesAPI")) { commonModule(this) }
    implementation(project(":emotesServer")) { commonModule(this) }
    api(project(":emotesMc")) { commonModule(this) }

    api("com.zigythebird.playeranim:PlayerAnimationLibCommon:${project["playeranimlib_version"]}")
    implementation("com.zigythebird.playeranim:PlayerAnimationLibCore:${project["playeranimlib_version"]}") {
        fabricPomCompile(this)
        neoforgePomCompile(this)
    }
    implementation("net.raphimc:NoteBlockLib:${project["noteblocklib_version"]}") {
        isTransitive = false

        platformInclude(this)
        fabricPomCompile(this)
        neoforgePomCompile(this)
    }

    // Third-party
    compileOnly("com.blamejared.searchables:Searchables-common-26.1.2:${project["searchables_version"]}") {
        isTransitive = false
    }

    // Fabric
    "fabricImplementation"(fabricApi.fabricModule("fabric-networking-api-v1", project["fabric_api_version"])) { fabricPomCompile(this) }
    "fabricImplementation"(fabricApi.fabricModule("fabric-key-mapping-api-v1", project["fabric_api_version"])) { fabricPomCompile(this) }
    "fabricImplementation"(fabricApi.fabricModule("fabric-lifecycle-events-v1", project["fabric_api_version"])) { fabricPomCompile(this) }
    "fabricImplementation"(fabricApi.fabricModule("fabric-rendering-v1", project["fabric_api_version"])) { fabricPomCompile(this) }

    "fabricRuntimeOnly"(fabricApi.fabricModule("fabric-screen-api-v1", project["fabric_api_version"]))
    "fabricCompileOnly"("com.terraformersmc:modmenu:${project["modmenu_version"]}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    "fabricImplementation"("me.lucko:fabric-permissions-api:${project["fabric_permissions_api"]}") {
        // fabricPomCompile(this)
    }

    "fabricImplementation"("com.zigythebird.playeranim:PlayerAnimationLibFabric:${project["playeranimlib_version"]}") {
        fabricPomCompile(this)
    }

    "fabricImplementation"("com.blamejared.searchables:Searchables-fabric-26.1.2:${project["searchables_version"]}") {
        isTransitive = false
    }

    /*"fabricRuntimeOnly"("com.zigythebird.bendable_cuboids:BendableCuboidsFabric:${project["bendablecuboids_version"]}") {
        isTransitive = false
        fabricPomCompile(this)
    }*/

    "fabricRuntimeOnly"("org.redlance.dima_dencep.mods:TranslationFallbacksFabric:${project["translationfallbacks_version"]}") {
        "fabricInclude"(this)
        fabricPomCompile(this)
    }

    // Neoforge
    "neoforgeImplementation"("com.zigythebird.playeranim:PlayerAnimationLibNeo:${project["playeranimlib_version"]}") {
        neoforgePomCompile(this)
    }

    /*modRuntimeOnly("com.zigythebird.bendable_cuboids:BendableCuboidsNeo:${project["bendablecuboids_version"]}") {
        isTransitive = false
        neoforgePomCompile(this)
    }*/

    "neoforgeRuntimeOnly"("org.redlance.dima_dencep.mods:TranslationFallbacksNeo:${project["translationfallbacks_version"]}") {
        "neoforgeInclude"(this)
        neoforgePomCompile(this)
    }
}

listOf("processResources", "processFabricResources", "processNeoforgeResources").forEach { name ->
    tasks.named<ProcessResources>(name) {
        inputs.property("version", version)
        inputs.property("description", mod_description)

        filesMatching(listOf("META-INF/neoforge.mods.toml", "fabric.mod.json")) {
            expand("version" to version, "description" to mod_description)
        }
    }
}

tasks.shadowJar {
    configurations = listOf(commonModule)

    from(tasks.named<AbstractArchiveTask>("remapFabricJar").map { zipTree(it.archiveFile) })
    from(tasks.named<AbstractArchiveTask>("remapNeoforgeJar").map { zipTree(it.archiveFile) })

    archiveBaseName.set("${archives_base_name}-for-MC${minecraft_version}")
    archiveClassifier.set("")

    // Services
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    mergeServiceFiles()

    // Fix fabric jij
    filesMatching("META-INF/jarjar/*.jar") {
        path = path.replace("META-INF/jarjar/", "META-INF/jars/")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    filesMatching("META-INF/jarjar/metadata.json") {
        filter { it.replace("META-INF/jarjar/", "META-INF/jars/") }
    }
}

machete {
    tasks.set(setOf("shadowJar"))
    sourceFileStriping.enabled = true

    png.compressionLevel.set(9)
    png.compressorIterations.set(32)
    png.enabled = false
}

tasks.jar {
    archiveClassifier.set("dev")
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

listOf("fabric", "neoforge").forEach { name ->
    tasks.register<Jar>("${name}SourcesJar") {
        from(sourceSets.main.get().allSource)
        from(sourceSets.getByName(name).allSource)

        archiveBaseName.set(rootProject.archives_base_name)
        archiveClassifier.set("${name}-sources")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenCommon") {
            artifactId = "archCommon"

            artifact(tasks.jar) {
                classifier = ""
            }
            artifact(tasks.sourcesJar)

            addDeps(project, configurations.implementation.get(), "compile")
            addDeps(project, configurations.api.get(), "compile")

            withCustomPom("archCommon", "Minecraft Emotecraft Architectury common module")
        }

        register<MavenPublication>("mavenFabric") {
            artifactId = "emotesFabric"

            artifact(tasks.getByName("remapFabricJar")) {
                classifier = ""
            }
            artifact(tasks.getByName("fabricSourcesJar")) {
                classifier = "sources"
            }

            addDeps(project, fabricPomCompile, "compile")

            withCustomPom("emotesFabric", "")
        }

        register<MavenPublication>("mavenNeo") {
            artifactId = "emotesNeo"

            artifact(tasks.getByName("remapNeoforgeJar")) {
                classifier = ""
            }
            artifact(tasks.getByName("neoforgeSourcesJar")) {
                classifier = "sources"
            }

            addDeps(project, neoforgePomCompile, "compile")

            withCustomPom("emotesNeo", "")
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
    modLoaders.add("neoforge")

    file.set(tasks.shadowJar.get().archiveFile)

    type = ReleaseType.of(releaseType)
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        announcementTitle = "Modrinth (Fabric/NeoForge)"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.addAll(release_minecraft_versions)
        version = mod_version
        displayName = "Emotecraft $mod_version for ${removePreRc(minecraft_version)}"

        // requires("fabric-api")
        requires("player-animation-library")
        optional("bendable-cuboids")
        optional("searchables")
        // optional("fabric-permissions-api")
    }

    curseforge("curseforgeFabric") {
        announcementTitle = "CurseForge (Fabric/NeoForge)"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id_fabric")
        projectSlug = providers.gradleProperty("curseforge_slug_fabric")
        changelogType = "markdown"
        displayName = "Emotecraft $mod_version for ${removePreRc(minecraft_version)}"
        minecraftVersions.addAll(curseforge_minecraft_versions)

        javaVersions.add(project.java_version)
        clientRequired = true
        serverRequired = true

        // requires("fabric-api")
        requires("player-animation-library")
        optional("bendable-cuboids")
        optional("searchables")
    }

    /*curseforge("curseforgeNeo") {
        announcementTitle = "CurseForge (Fabric/NeoForge)"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id_forge")
        projectSlug = providers.gradleProperty("curseforge_slug_forge")
        changelogType = "markdown"
        displayName = "Emotecraft $mod_version for ${removePreRc(minecraft_version)}"
        minecraftVersions.addAll(curseforge_minecraft_versions)

        javaVersions.add(project.java_version)
        clientRequired = true
        serverRequired = true

        requires("player-animation-library")
        optional("bendable-cuboids")
        optional("searchables")
    }*/
}

afterEvaluate {
    val optimizeOutputsOfShadowJar = tasks.getByName("optimizeOutputsOfShadowJar")

    tasks.publishMods.configure { dependsOn(optimizeOutputsOfShadowJar) }
    publishMods.platforms
        .map { platform -> project.tasks.getByName(platform.taskName) as PublishModTask }
        .forEach { it.dependsOn(optimizeOutputsOfShadowJar) }
}
