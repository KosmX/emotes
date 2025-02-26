@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
}

architectury {
    minecraft = minecraft_version
}

version = mod_version

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

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
