# Player animation tools

The `.blend` files are [Blender](https://www.blender.org/) projects.  
To use `emote_creator_bend_item_scale.blend` you need Blender version at least 5.0. For `emote_creator` and `emote_creator_bend` you can use Blender version from 2.83 up to 4.x. 
[Emotecraft wiki](https://docs.zigythebird.com/emotecraft/creatingemotes/) if you're stuck.  

`.bbmodel` files are models for [Blockbench](https://blockbench.net/). You can use them as well.  
To use them, you'll need to install [GeckoLib](https://geckolib.com/) plugin first.   
The Blockbench emotes support is not very good on MC versions <1.21.7

Models labled with `_bend` allow you to bend some bones like in Minecraft Story Mode, and the rest of the labels should be self explanatory.
Keep in mind that the visual for bending is incorrect in Blender/Blockbench, there won't be any gaps created by bending a bone in-game.  
All Blockbench models support scaling.
> [!WARNING]
> Scale animation will be visible only on Minecraft version 1.21.4+

> [!CAUTION]  
>  BENDING ON THE Z AXIS IS NOT SUPPORTED!!!  
>  And by Z axis I mean the one facing forward, I need to specify this since Blender uses a different coordinate system than MC.  
>  Make sure every bone's Z rotation value is always set to 0 in Blockbench and the same for Y rotation values in Blender!  

> [!TIP]
>  It's possible to add custom bones to a Blockbench model in order to animate player accessories IF it's supported by playerAnimator or another mod.  
>  For example you can add a bone called elytra to the model and animate the elytra that way!  
>  Cape rotations are also applied to elytras, but there won't be any bending.  
>  The elytra bone's priority is greater than cape bone's for animating elytra but both can influence the elytra at the same time.  

### If you don't like these
You can create your own program or edit the file by hand   
The emote format documentation is [here](https://github.com/KosmX/emotes/wiki/Emote.json)  
[Here](https://github.com/bigguy345/Blender-Minecraft-Animation/tree/main) is a Blender addon that lets you import and save animations + bend limbs on multiple axes.  
