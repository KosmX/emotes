package io.github.kosmx.emotes.arch.network.neoforge;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NetworkPlatformToolsImpl {
    public static boolean canSendPlay(ServerPlayer player, Identifier channel) {
        return player.connection.hasChannel(channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl packetListener, Identifier channel) {
        return packetListener.hasChannel(channel);
    }

    public static Collection<ServerPlayer> getTrackedBy(Entity entity) {
        ChunkMap.TrackedEntity tracked = ((ServerLevel) entity.level()).getChunkSource().chunkMap.entityMap.get(entity.getId());
        return tracked.seenBy.stream()
                .map(ServerPlayerConnection::getPlayer)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
