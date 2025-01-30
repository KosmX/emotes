import me.modmuss50.mpp.ReleaseType

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
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
    named("developmentNeoForge").configure { extendsFrom(common) }
}

dependencies {
    neoForge("net.neoforged:neoforge:${neoforge_version}")

    commonModule(project(":executor")) { isTransitive = false }
    commonModule(project(":emotesAPI")) { isTransitive = false }
    commonModule(project(":emotesServer")) { isTransitive = false }
    commonModule(project(":emotesAssets")) { isTransitive = false }
    commonModule(project(path = ":emotesMc", configuration = "namedElements")) { isTransitive = false }

    modImplementation("dev.kosmx.player-anim:player-animation-lib-forge:${player_animator_version}") {
        include(this)
        pomCompile(this)
    }

    pomCompile(project(":emotesAssets"))
    pomCompile(project(":minecraft:archCommon"))

    common(project(path = ":minecraft:archCommon", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(
        project(
            path = ":minecraft:archCommon",
            configuration = "transformProductionNeoForge"
        )
    ) { isTransitive = false }
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("description", mod_description)

    filesMatching("META-INF/neoforge.mods.toml") {
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

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveClassifier.set("")
}

tasks.remapJar {
    atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("")
}

components.getByName<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) {
        skip()
    }
}

tasks.register<Jar>("devJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
}

tasks.build {
    dependsOn("devJar")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesNeo"

            // add all the jars that should be included when publishing to maven
            artifact(tasks.named("devJar"))
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
    modLoaders.add("neoforge")
    file.set(tasks.remapJar.get().archiveFile)
    type = ReleaseType.of(releaseType)
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
        version = "${mod_version}+${minecraft_version}-forge"

        embeds("playeranimator")
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id_forge")
        changelogType = "markdown"
        displayName = base.archivesName.get() + "-$mod_version"
        minecraftVersions.add(minecraft_version)

        embeds("playeranimator")
    }
}
