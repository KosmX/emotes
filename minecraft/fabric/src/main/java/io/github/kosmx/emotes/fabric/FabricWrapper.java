package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.arch.ServerCommands;
import io.github.kosmx.emotes.arch.network.CommonServerNetworkHandler;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.fabric.executor.FabricEmotesMain;
import io.github.kosmx.emotes.fabric.network.ServerNetworkStuff;
import io.github.kosmx.emotes.main.MainLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class FabricWrapper implements ModInitializer {

    public static final Logger logger = LoggerFactory.getLogger(CommonData.MOD_ID);
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        EmoteInstance.instance = new FabricEmotesMain();
        MainLoader.main(null);
        setupFabric(); //Init keyBinding, networking etc...

    }

    private static void setupFabric(){

        ServerNetworkStuff.init();
        subscribeEvents();
    }


    public static void log(Level level, String msg){
        if (level.intValue() <= Level.INFO.intValue()) {
            logger.debug(msg);
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            logger.warn(msg);
        } else {
            logger.error(msg);
        }
    }

    public static void log(Level level, String msg, Throwable throwable){
        if (level.intValue() <= Level.INFO.intValue()) {
            logger.debug(msg, throwable);
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            logger.warn(msg, throwable);
        } else {
            logger.error(msg, throwable);
        }
    }

    private static void subscribeEvents() {
        CommandRegistrationCallback.EVENT.register(ServerCommands::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER_INSTANCE = server);
    }
}
