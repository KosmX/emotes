package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.fabric.network.ServerNetworkStuff;
import io.github.kosmx.emotes.main.MainLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class FabricWrapper implements ModInitializer {
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        MainLoader.main(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        setupFabric(); //Init keyBinding, networking etc...
    }

    private static void setupFabric(){
        ServerNetworkStuff.init();
        subscribeEvents();
    }

    private static void subscribeEvents() {
        CommandRegistrationCallback.EVENT.register(ServerCommands::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER_INSTANCE = server);
    }
}
