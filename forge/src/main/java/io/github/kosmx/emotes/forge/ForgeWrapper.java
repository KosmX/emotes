package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.forge.executor.ForgeEmotesMain;
import io.github.kosmx.emotes.forge.network.ServerNetwork;
import io.github.kosmx.emotes.main.MainLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.logging.Logger;

@Mod("emotecraft")
public class ForgeWrapper {
    public static final Logger LOGGER = Logger.getLogger("Emotecraft");


    public ForgeWrapper(){
        EmoteInstance.instance = new ForgeEmotesMain();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }


    private void setup(final FMLCommonSetupEvent event){
        MainLoader.main(new String[]{"FML"});
        if(FMLLoader.getDist() == Dist.CLIENT){
            ClientInit.initClient();
        }
        ServerNetwork.instance.init();

    }
}
