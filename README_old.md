[![](https://img.shields.io/discord/737216980095991838?label=Discord)](https://discord.gg/6NfdRuE)
[![](http://cf.way2muchnoise.eu/title/emotecraft.svg)![](http://cf.way2muchnoise.eu/versions/emotecraft_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/emotecraft)

##
Minecraft mod to add emotes to the game like in Bedrock Edition...
Just it's open emoteFormat and free unlike that :D
## Minecraft emotes

[User manual](https://kosmx.gitbook.io/emotecraft/)  
[Mod code documentation](https://github.com/KosmX/emotes/wiki)


Set-up dev environment and compile
```bash
git clone https://github.com/KosmX/emotes.git
```
You should set it up as a [Fabric mod](https://fabricmc.net/wiki/tutorial:setup)

Emotecraft dependencies:  
Note: some dependencies are from jcenter `jcenter()`
<br>
[Fabric-api](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
```groovy
modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
```
[Mod Menu](https://github.com/TerraformersMC/ModMenu)
```groovy
modImplementation "io.github.prospector:modmenu:${project.modmenu_version}"
include "io.github.prospector:modmenu:${project.modmenu_version}"
```
[Cloth Config](https://github.com/shedaniel/cloth-config)
```groovy
modImplementation "me.shedaniel.cloth:config-2:${project.cloth_version}"
include "me.shedaniel.cloth:config-2:${project.cloth_version}"
```
[bendy-lib](https://github.com/kosmx/bendy-lib)  
```groovy
modImplementation "com.kosmx.bendylib:bendy-lib:${project.bendylib_version}"
include "com.kosmx.bendylib:bendy-lib:${project.bendylib_version}"
```  
    
[Perspective Mod Redux](https://github.com/BackportProjectMC/PerspectiveModRedux)
```groovy
modImplementation "pm.c7.perspective:PerspectiveModRedux:0.0.5"
```
Perpective Mod Redux is not in any maven repo, you need to publish it to your local repo
```groovy
mavenLocal()
```
To do it, you need to git it  
`git clone https://github.com/BackportProjectMC/PerspectiveModRedux.git`  
edit the build.gradle with the following
```diff
@@ -77,6 +77,9 @@
 			artifact(jar) {
 				builtBy remapJar
 			}
+			artifact(remapJar){
+				builtBy remapJar
+			}
 			artifact(sourcesJar) {
 				builtBy remapSourcesJar
 			}
@@ -86,6 +89,6 @@
 	// select the repositories you want to publish to
 	repositories {
 		// uncomment to publish to the local maven
-		// mavenLocal()
+		mavenLocal()
 	}
 }
```
<br>
If you want to implement the mod, you won't need Perspective Mod Redux to build your mod