package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.fabric.executor.FabricEmotesMain;
import io.github.kosmx.emotes.fabric.network.ServerNetwork;
import io.github.kosmx.emotes.main.MainLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FabricWrapper implements ModInitializer {

    public static final Logger logger = Logger.getLogger(CommonData.MOD_NAME);
    public static MinecraftServer SERVER_INSTANCE;

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
        subscribeEvents();
    }

    public static void log(Level level, String msg){
        logger.log(level, msg);
    }

    private static void subscribeEvents() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            SERVER_INSTANCE = server; //keep it for later use
        });
    }
}
