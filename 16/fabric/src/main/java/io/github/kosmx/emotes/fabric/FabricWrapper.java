package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.fabric.executor.FabricEmotesMain;
import io.github.kosmx.emotes.fabric.network.ServerNetwork;
import io.github.kosmx.emotes.main.MainLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FabricWrapper implements ModInitializer {

    public static final Logger logger = Logger.getLogger(CommonData.MOD_NAME);


    @Override
    public void onInitialize() {
        EmoteInstance.instance = new FabricEmotesMain();
        MainLoader.main(null);
        setupFabric(); //Init keyBinding, networking etc...

    }

    private static void setupFabric(){
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientInit.initClient();
        }

        ServerNetwork.instance.init();

    }

    public static void log(Level level, String msg){
        logger.log(level, msg);
    }
}
