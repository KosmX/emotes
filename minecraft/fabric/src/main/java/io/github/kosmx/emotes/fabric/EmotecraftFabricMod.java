package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.fabric.network.ServerNetworkStuff;
import io.github.kosmx.emotes.main.EmotecraftMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class EmotecraftFabricMod extends EmotecraftMod implements ModInitializer {
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        super.onInitialize(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);

        ServerNetworkStuff.init();

        CommandRegistrationCallback.EVENT.register(ServerCommands::register);
        EntityTrackingEvents.START_TRACKING.register(this::onStartTracking);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER_INSTANCE = server);
    }
}
