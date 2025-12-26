package io.github.kosmx.emotes.arch.network.neoforge;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
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

public final class NetworkPlatformToolsImpl implements NetworkPlatformTools {
    @Override
    public boolean canSendPlay(ServerPlayer player, Identifier channel) {
        return player.connection.hasChannel(channel);
    }

    @Override
    public boolean canSendConfig(ServerConfigurationPacketListenerImpl packetListener, Identifier channel) {
        return packetListener.hasChannel(channel);
    }

    @Override
    public Collection<ServerPlayer> getTrackedBy(Entity entity) {
        ChunkMap.TrackedEntity tracked = ((ServerLevel) entity.level()).getChunkSource().chunkMap.entityMap.get(entity.getId());
        return tracked.seenBy.stream()
                .map(ServerPlayerConnection::getPlayer)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
