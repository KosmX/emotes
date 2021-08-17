package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.forge.executor.ForgeEmotesMain;
import io.github.kosmx.emotes.forge.network.ServerNetwork;
import io.github.kosmx.emotes.main.MainLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.logging.Logger;

@Mod(modid = "emotes")
public class ForgeWrapper {
    public static final Logger LOGGER = Logger.getLogger("Emotecraft");


    public ForgeWrapper(){

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    private void registerCore(final FMLPreInitializationEvent event){
        EmoteInstance.instance = new ForgeEmotesMain(event.getSide());
    }

    @Mod.EventHandler
    private void setup(final FMLInitializationEvent event){
        MainLoader.main(new String[]{"FML"});
        if(event.getSide() == Side.CLIENT){
            ClientInit.initClient();
        }
        ServerNetwork.instance.init();

    }
}
