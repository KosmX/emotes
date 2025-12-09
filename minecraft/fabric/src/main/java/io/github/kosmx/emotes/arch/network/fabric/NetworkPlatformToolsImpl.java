package io.github.kosmx.emotes.arch.network.fabric;

import io.github.kosmx.emotes.fabric.EmotecraftFabricMod;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

@SuppressWarnings("unused")
public class NetworkPlatformToolsImpl {
    public static boolean canSendPlay(ServerPlayer player, Identifier channel) {
        return ServerPlayNetworking.canSend(player, channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl player, Identifier channel) {
        return ServerConfigurationNetworking.canSend(player, channel);
    }

    public static Collection<ServerPlayer> getTrackedBy(Entity entity) {
        return PlayerLookup.tracking(entity);
    }

    public static MinecraftServer getServer() {
        return EmotecraftFabricMod.SERVER_INSTANCE;
    }
}
