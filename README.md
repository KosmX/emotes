# Emotecraft  
a.k.a. EmoteX 

## Download
When downloading the mod, please **only use** official downloads as others may be infected.  
Official project (only download the mod from here):
- [GitHub/KosmX/emotes](https://github.com/KosmX/emotes)
- [Modrinth/Emotecraft](https://modrinth.com/mod/emotecraft)
- [CurseForge/Emotecraft](https://www.curseforge.com/minecraft/mc-mods/emotecraft) and [CurseForge/Emotecraft (Forge)](https://www.curseforge.com/minecraft/mc-mods/emotecraft-forge)
- [maven.kosmx.dev](https://maven.kosmx.dev/io/github/kosmx/emotes/) this is for developers.
**Don't download it from any other source!**

## Development
---
How to build:
```bash
git clone https://github.com/KosmX/emotes.git
cd emotes
./gradlew build
```
  
### Using in your mod/modpack  

`Fabric` optionally depends on [**Mod Menu**](https://github.com/TerraformersMC/ModMenu)   and FabricMC mods: **Fabric-loader**, **Fabric-API**, **Minecraft**.

`Forge` version has no dependencies (except **Forge** and **Minecraft**)

### Emotes proxy
Emotecraft is doing the emote synchronization using a server-side mod.  
In some cases it's just impossible (like when playing on a community server)
   
This is where proxy API comes in as it can redirect communication when dedicated server-side mod isn't available.  
If the server has Emotecraft (in any form) it will use that instead of using proxies.  

To implement a proxy-mod, see [emotes-proxy-template](https://github.com/KosmX/emotes-proxy-template).  
Emotecraft will invoke the proxy instance when trying to send a message,  
and you can invoke Emotecraft's receiver when you received a message. 


Modules:
--------
`emotesAPI`: Common library used by Emotecraft, loader-independent, published as **emotesAPI**  
    you can find it in my private maven server: [`https://maven.kosmx.dev`](https://maven.kosmx.dev)  
`executor`: The interface to be implemented by loader  
`emotesMain`: Common assets
`emotesMc`: Common serverside Minecraft code
`emotesServer`: Server-side logic    
<br>
`archCommon`: Common (both Fabric and NeoForge) Minecraft dependent stuff. using [architectury](https://github.com/architectury/forgified-fabric-loom) loom  
`fabric`: Fabric implementation  
`neoforge`: NeoForge implementation  
`buildSrc`: Build logic utilities

### If you have any questions about the mod, you can find me on Discord
[![](https://img.shields.io/discord/737216980095991838?label=Discord)](https://discord.gg/6NfdRuE)
