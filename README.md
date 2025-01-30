<div align="center">

[![See on Modrinth - Emotecraft](https://img.shields.io/badge/See_on_Modrinth-Emotecraft-2ea44f?logo=modrinth)](https://modrinth.com/mod/emotecraft) 
[![See on CurseForge (Fabric) - Emotecraft](https://img.shields.io/badge/See_on_CurseForge-Emotecraft_(Fabric)-orange?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/emotecraft)
[![See on CurseForge (Forge/NeoForge) - Emotecraft](https://img.shields.io/badge/See_on_CurseForge-Emotecraft_(Forge/NeoForge)-orange?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/emotecraft-forge)

[![Discord](https://img.shields.io/discord/737216980095991838?label=Discord)](https://discord.gg/6NfdRuE)
[![GitHub Release](https://img.shields.io/github/v/release/KosmX/emotes)](https://github.com/KosmX/emotes/releases/latest)
</div>

# Emotecraft  
a.k.a. EmoteX 

## Download
When downloading the mod, please **only use** official downloads as others may be infected.  
Official project (only download the mod from here):
- [Github/KosmX/emotes](https://github.com/KosmX/emotes)
- [Modrinth/Emotecraft](https://modrinth.com/mod/emotecraft)
- [CurseForge/Emotecraft (Fabric)](https://www.curseforge.com/minecraft/mc-mods/emotecraft) and [CurseForge/Emotecraft (Forge/NeoForge)](https://www.curseforge.com/minecraft/mc-mods/emotecraft-forge)

**Don't download it from any other source!**

### Minecraft versions table
| Minecraft | Emotecraft |
|-----------|------------|
| 1.21.1    | 2.4.x      |
| 1.21.4    | 2.5.x      |

## Development

---
**Building from source:**
```bash
git clone https://github.com/KosmX/emotes.git
cd emotes
./gradlew build
```
**Adding repository:**   
Gradle Kotlin DSL:
```kotlin
maven("https://maven.kosmx.dev/")
```
  
### Using in your mod/modpack  

`Fabric` optionally depends on [**Mod Menu**](https://github.com/TerraformersMC/ModMenu) and FabricMC mods: **Fabric-Loader**, **Fabric-API**, **Minecraft**.  
`Forge` version depends on [**PlayerAnimator**](https://github.com/KosmX/minecraftPlayerAnimator)   
**bendy-lib** is compiled into the forge version  
`NeoForge` version has no dependencies (except **NeoForge** and **Minecraft**)

### Emotes proxy
Emotecraft is doing the emote synchronization using a server-side mod.  
In some cases it's just impossible (like when playing on a community server)
   
This is where proxy API comes in as it can redirect communication when dedicated server-side mod isn't available.  
If the server has Emotecraft (in any form) it will use that instead of using proxies.  

To implement a proxy-mod, see [emotes-proxy-template](https://github.com/KosmX/emotes-proxy-template).  
Emotecraft will invoke the proxy instance when trying to send a message,  
and you can invoke Emotecraft's receiver when you received a message. 


### Modules:
`emotesAPI`: Common library used by Emotecraft, loader-independent  
`executor`: The interface to be implemented by loader  
`emotesAssets`: Common assets
`emotesMc`: Common serverside Minecraft code  
`emotesServer`: Server-side logic  
`archCommon`: Common (both Fabric and NeoForge) Minecraft dependent stuff. using [architectury](https://github.com/architectury/architectury-loom) loom  
`fabric`: Fabric implementation  
`neoforge`: NeoForge implementation  
`buildSrc`: Build logic utilities

_More info can be found in ABOUT.md files inside module directory_

