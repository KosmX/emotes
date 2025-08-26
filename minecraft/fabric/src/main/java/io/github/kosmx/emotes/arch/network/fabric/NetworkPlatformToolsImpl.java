package io.github.kosmx.emotes.arch.network.fabric;

import io.github.kosmx.emotes.fabric.EmotecraftFabricMod;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class NetworkPlatformToolsImpl {
    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        return ServerPlayNetworking.canSend(player, channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl player, ResourceLocation channel) {
        return ServerConfigurationNetworking.canSend(player, channel);
    }

    public static MinecraftServer getServer() {
        return EmotecraftFabricMod.SERVER_INSTANCE;
    }
}
