pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.redlance.org/public")
        gradlePluginPortal()
    }
}

rootProject.name = "emotecraft"

include("emotesAPI")
include("emotesServer")
include("emotesAssets")
include("emotesMc")

//Minecraft 1.20 version
include("minecraft")
include("minecraft:archCommon")
include("minecraft:fabric")
include("minecraft:neoforge")

// Paper plugin
include("paper")
