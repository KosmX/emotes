import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.architectury.loom") version "1.13.467" apply false
    id("architectury-plugin") version "3.4.162" apply true
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "io.github.kosmx.emotes"

    repositories {
        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"
            content {
                includeGroup("maven.modrinth")
            }
        }
        maven("https://maven.terraformersmc.com/") {
            name = "TerraformersMC maven"
        }
        maven("https://maven.blamejared.com") {
            name = "BlameJared Maven"
        }
        maven("https://repo.redlance.org/public")
        maven("https://libraries.minecraft.net") {
            content { // Fix issue with lwjgl-freetype not being found on macOS
                includeModule("org.lwjgl", "lwjgl-freetype")
            }
        }
        maven("https://maven.neoforged.net/releases")
        mavenLocal()
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.release = project.java_version.majorVersion.toInt()
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }
}

//---------------- Publishing ----------------

releaseType = ENV["RELEASE_TYPE"] ?: "alpha"
changes = ENV["CHANGELOG"]?.replace("\\n", "\n") ?: ""
mod_version = version_base

if (releaseType != "stable") {
    mod_version = "${version_base}-${releaseType[0]}.${ENV["BUILD_NUMBER"]?.let { "build.$it" } ?: getGitShortRevision()}"
}
version = mod_version

shouldPublishMaven = providers.environmentVariable("KOSMX_TOKEN").getOrElse("").isNotBlank()
        && !gradle.startParameter.isDryRun

publishMods {
    changelog = changes
    type = ReleaseType.of(releaseType)
    dryRun = gradle.startParameter.isDryRun

    github {
        tagName = mod_version
        commitish = getGitRevision()
        repository = providers.environmentVariable("GITHUB_REPOSITORY").orElse("KosmX/emotes")
        accessToken = providers.environmentVariable("GH_TOKEN")
        displayName = "Emotecraft-${mod_version}"
        allowEmptyFiles = true
    }
}

val ds = publishDiscord {
    onlyIf {
        val explicit = gradle.startParameter.taskNames.contains(name)
        if (explicit) return@onlyIf true

        val mods = gradle.taskGraph.allTasks.filter { it.name == "publishMods" }
        mods.isNotEmpty() && mods.all { it.state.failure == null}
    }

    username = "Emotecraft Updates"
    content = "<@&926902263941849118>"
    url = providers.environmentVariable("DISCORD_WEBHOOK")

    val changelog = changes.replace("<br>", "  \n")
    embed {
        color = kotlin.random.Random.nextInt(0x000000, 0x1000000)
        title = "Emotecraft $mod_version for Minecraft $minecraft_version is out!"
        description = "Changes:  \n$changelog"
        thumbnail("https://raw.githubusercontent.com/KosmX/emotes/d97b2df4ab59bbd2740f30497e96f92cb643b2df/emotesAssets/src/main/resources/emotecraft_mod_logo.png")
        timestamp(System.currentTimeMillis())
    }

    links {
        from(":minecraft:neoforge", "modrinth")
        from(":minecraft:fabric", "modrinth")

        val paper = project(":paper")
        from(paper, "modrinth")
        nextRow()

        from(":minecraft:neoforge", "curseforge")
        from(":minecraft:fabric", "curseforge")

        /*val hangarProjectName = providers.gradleProperty("hangarProjectName")
            .getOrElse("dima_dencep/emotecraft")
        val ver = "${paper.mod_version}+${paper.minecraft_version}-paper"
        val hangarLink = "https://hangar.papermc.io/$hangarProjectName/versions/$ver"
        nextRow()
        custom("Hangar (Paper)", hangarLink, HANGAR_EMOJI)*/
    }
}

allprojects {
    tasks.matching { it.name == "publishMods" }.configureEach {
        finalizedBy(ds)
    }
}
