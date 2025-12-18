@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin")
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
            parchment("org.parchmentmc.data:parchment-1.21.10:${parchment_version}@zip")
        })
    }
}
