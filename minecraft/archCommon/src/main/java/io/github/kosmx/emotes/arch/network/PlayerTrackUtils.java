package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.EntityTrackerAccessor;
import io.github.kosmx.emotes.arch.mixin.ServerChunkCacheAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerTrackUtils {
    public static Set<ServerPlayer> getTrackedBy(ServerPlayer player) {
        EntityTrackerAccessor entityTracker = getEntityTracker(player);
        if (entityTracker != null) {
            return entityTracker.getPlayersTracking().stream()
                    .map(ServerPlayerConnection::getPlayer)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Collections.emptySet();
    }

    public static boolean isTrackedBy(ServerPlayer player, ServerPlayer other) {
        EntityTrackerAccessor entityTracker = getEntityTracker(player);
        return entityTracker != null && entityTracker.getPlayersTracking().contains(other.connection);
    }

    public static EntityTrackerAccessor getEntityTracker(ServerPlayer player) {
        ServerChunkCacheAccessor source = (ServerChunkCacheAccessor) player.level().getChunkSource().chunkMap;
        return source.getTrackedEntity().get(player.getId());
    }
}
