package io.github.kosmx.emotes.arch.network.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NetworkPlatformToolsImpl {
    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        return player.connection.hasChannel(channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl packetListener, ResourceLocation channel) {
        return packetListener.hasChannel(channel);
    }

    public static Collection<ServerPlayer> getTrackedBy(ServerPlayer player) {
        ChunkMap.TrackedEntity tracked = player.level().getChunkSource().chunkMap.entityMap.get(player.getId());
        return tracked.seenBy.stream()
                .map(ServerPlayerConnection::getPlayer)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
