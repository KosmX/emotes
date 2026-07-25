# Player Animation Tools

The `.blend` files are [Blender](https://www.blender.org/) projects.  

`emote_creator.blend` is the latest rig for blender, it has the most features. It is made for Blender 5.2+ and intended to work only on the latest version of PAL (1.2.5+mc.26.2 at the time this is written)! Other rigs work fine on versions lower.
Read [Emotecraft wiki](https://docs.zigythebird.com/emotecraft/creatingemotes/) to learn more about `emote_creator.blend`.

`.bbmodel` files are models for [Blockbench](https://blockbench.net/). You can use them as well.  
To use them, you'll need to install the [GeckoLib](https://geckolib.com/) Blockbench plugin first.   
The Blockbench emotes support is not very good on MC versions <1.21.7

Models labled with `_bend` allow you to bend some bones like in Minecraft Story Mode, and the rest of the labels should be self explanatory.
Keep in mind that the visual for bending is incorrect in Blender/Blockbench, there won't be any gaps created by bending a bone in-game.  
All Blockbench models support scaling.
> [!WARNING]
> Scale animation will be visible only on Minecraft version 1.21.4+

> [!TIP]
>  It's possible to add custom bones to a Blockbench model in order to animate player accessories IF it's supported by playerAnimator or another mod.  
>  For example you can add a bone called elytra to the model and animate the elytra that way!  
>  Cape rotations are also applied to elytras, but there won't be any bending.  
>  The elytra bone's priority is greater than cape bone's for animating elytra but both can influence the elytra at the same time.  

### If you don't like these
You can create your own program or edit the file by hand   
The emote format documentation is [here](https://github.com/KosmX/emotes/wiki/Emote.json)  
[Here](https://github.com/bigguy345/Blender-Minecraft-Animation/tree/main) is a Blender addon that lets you import and save animations + bend limbs on multiple axes.  
