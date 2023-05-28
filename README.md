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
how to build
```bash
git clone https://github.com/KosmX/emotes.git
cd emotes
./gradlew build
```
You can use `collectArtifacts` task to copy the mod files into an artifacts directory.  
```bash
./gradlew collectArtifact
cd artifacts
```
  
### Using in your mod/modpack  

`Fabric` depends on [**bendy-lib**](https://github.com/KosmX/bendy-lib), optionally [**Mod Menu**](https://github.com/TerraformersMC/ModMenu)   and FabricMC mods: **Fabric-loader**, **Fabric-API**, **Minecraft**.  


`Forge` version has no dependencies (except **Forge** and **Minecraft**)  
**bendy-lib** is compiled into the forge version  

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
`emotesAPI`: common library used by Emotecraft, has no dependencies, published as **emotesAPI**  
    you can find it in my private maven server: [`https://maven.kosmx.dev`](https://maven.kosmx.dev)  
`executor`: the interface to be implemented to the modloader+MC version  
`emotesMain`: Main client-side logic    
`emotesServer`: Server-side logic    
<br>
`archCommon`: common (both Fabric and Forge) Minecraft dependent stuff. using [architectury](https://github.com/architectury/forgified-fabric-loom) loom  
`fabric`: latest fabric implementation  
`forge`: latest Forge implementation  

### If you have any questions about the mod, you can find me on Discord
[![](https://img.shields.io/discord/737216980095991838?label=Discord)](https://discord.gg/6NfdRuE)
