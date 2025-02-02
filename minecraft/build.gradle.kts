@file:Suppress("UnstableApiUsage")

import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

architectury {
    minecraft = minecraft_version
}

version = mod_version

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName = "${archives_base_name}-${name}-for-MC${minecraft_version}"
    version = mod_version

    val loom = extensions.getByType(LoomGradleExtensionAPI::class)

    loom.silentMojangMappingsLicense()

    dependencies {
        configurations.getByName("minecraft")("com.mojang:minecraft:${minecraft_version}")
        configurations.getByName("mappings")(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${minecraft_version}:${parchment_version}@zip")
        })
    }
}

publishMods {
    type = ReleaseType.of(releaseType)
    changelog = changes
    dryRun = gradle.startParameter.isDryRun

    val fabric = project("fabric")
    val neoforge = project("neoforge")
    val fabricJar = file(fabric.tasks.getByName<AbstractArchiveTask>("remapJar").archiveFile)
    val neoforgeJar = file(neoforge.tasks.getByName<AbstractArchiveTask>("remapJar").archiveFile)

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
        additionalFiles.from(fabricJar, neoforgeJar)
    }

    val modrinthOptions = modrinthOptions {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.add(minecraft_version)
    }

    modrinth("modrinthNeoForge") {
        from(modrinthOptions)
        announcementTitle = "Modrinth/NeoForge"
        modLoaders.add("neoforge")
        displayName = mod_version
        version = "${mod_version}+${minecraft_version}-forge"
        file = neoforgeJar

        embeds("playeranimator")
    }

    modrinth("modrinthFabric") {
        from(modrinthOptions)
        announcementTitle = "Modrinth/Fabric"
        modLoaders.add("fabric")
        modLoaders.add("quilt")
        displayName = mod_version
        version = "${mod_version}+${minecraft_version}-fabric"
        file = fabricJar

        requires("fabric-api")
        embeds("playeranimator")
    }

    val cfOptions = curseforgeOptions {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        changelogType = "markdown"
        minecraftVersions.add(minecraft_version)
    }

    curseforge("curseforgeNeoForge") {
        from(cfOptions)
        announcementTitle = "CurseForge/NeoForge"
        modLoaders.add("neoforge")
        projectId = providers.gradleProperty("curseforge_id_forge")
        projectSlug = providers.gradleProperty("curseforge_slug_forge")
        displayName = neoforge.base.archivesName.get() + "-$mod_version"
        file = neoforgeJar

        embeds("playeranimator")
    }

    curseforge("curseforgeFabric") {
        from(cfOptions)
        announcementTitle = "CurseForge/Fabric"
        modLoaders.add("fabric")
        modLoaders.add("quilt")
        projectId = providers.gradleProperty("curseforge_id_fabric")
        projectSlug = providers.gradleProperty("curseforge_slug_fabric")
        displayName = fabric.base.archivesName.get() + "-$mod_version"
        file = fabricJar

        requires("fabric-api")
        embeds("playeranimator")
    }
}
