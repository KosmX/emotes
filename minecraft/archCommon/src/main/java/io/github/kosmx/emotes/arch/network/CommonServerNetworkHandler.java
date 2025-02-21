package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.mixin.ServerChunkCacheAccessor;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<Player> {
    public static CommonServerNetworkHandler instance = new CommonServerNetworkHandler();

    private CommonServerNetworkHandler() {} // make ctor private for singleton class

    public void init() {
    }

    public void receiveMessage(byte[] bytes, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(bytes, player, getHandler(serverPlayer.connection));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static IServerNetworkInstance getHandler(ServerGamePacketListenerImpl handler) {
        return ((EmotesMixinNetwork)handler).emotecraft$getServerNetworkInstance();
    }

    public void receiveStreamMessage(byte[] bytes, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            receiveStreamMessage(serverPlayer, getHandler(serverPlayer.connection), ByteBuffer.wrap(bytes));
        }
    }

    public void receiveStreamMessage(ServerPlayer player, IServerNetworkInstance handler, ByteBuffer buf) {
        try
        {
            if (((EmotesMixinNetwork)handler).emotecraft$getServerNetworkInstance().allowEmoteStreamC2S()) {
                var packet = ((AbstractServerNetwork)handler).receiveStreamChunk(buf);
                if (packet != null) {
                    receiveMessage(packet.array(), player, handler);
                }
            } else {
                handler.disconnect("Emote stream is disabled on this server");
            }
        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to receive packet!", e);
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected Player getPlayerFromUUID(UUID player) {
        return NetworkPlatformTools.getServer().getPlayerList().getPlayer(player);
    }

    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getId();
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player sourcePlayer) {
        if (!(sourcePlayer instanceof ServerPlayer player)) {
            return null;
        }

        return ((EmotesMixinNetwork)player.connection).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(@Nullable NetData data, Player player) {
        getTrackedPlayers(player).forEach(target -> {
            if (target != player) {
                try {
                    if (data != null && NetworkPlatformTools.canSendPlay(target, NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                        playerNetwork.sendMessage(new EmotePacket.Builder(data), null);
                    }
                } catch (IOException e) {
                    LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
                }
            }
        });
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        if (!(player instanceof ServerPlayer sourcePlayer)) {
            return;
        }

        try {
            var targetPlayer = sourcePlayer.server.getPlayerList().getPlayer(target);
            if (targetPlayer != null && targetPlayer.getChunkTrackingView().contains(sourcePlayer.chunkPosition())) {
                getPlayerNetworkInstance(targetPlayer).sendMessage(new EmotePacket.Builder(data), null);
            }

        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
        }
    }

    @Override
    protected void sendForPlayer(NetData data, Player ignore, UUID target) {
        try {
            Player player = getPlayerFromUUID(target);
            IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(player);

            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
            playerNetwork.sendMessage(packetBuilder, null);
        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
        }
    }

    private Collection<ServerPlayer> getTrackedPlayers(Entity entity) {
        var level = entity.level().getChunkSource();
        if (level instanceof ServerChunkCache chunkCache) {
            ServerChunkCacheAccessor storage = (ServerChunkCacheAccessor) chunkCache.chunkMap;

            var tracker = storage.getTrackedEntity().get(entity.getId());
            if (tracker != null) {
                return tracker.getPlayersTracking()
                        .stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
            }
            return Collections.emptyList();
        }
        throw new IllegalArgumentException("server function called on logical client");
    }
}
