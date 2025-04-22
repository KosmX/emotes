package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<ServerPlayer> {
    public void receiveMessage(byte[] bytes, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(bytes, serverPlayer, getHandler(serverPlayer.connection));
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
                player.connection.disconnect(Component.literal("Emote stream is disabled on this server"));
            }
        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to receive packet!", e);
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(ServerPlayer player) {
        return player.getUUID();
    }

    @Override
    protected ServerPlayer getPlayerFromUUID(UUID player) {
        return NetworkPlatformTools.getServer().getPlayerList().getPlayer(player);
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(ServerPlayer player) {
        return ((EmotesMixinNetwork)player.connection).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, ServerPlayer player) {
        for (ServerPlayer target : PlayerTrackUtils.getTrackedBy(player)) {
            if (target == player) continue;

            try {
                if (NetworkPlatformTools.canSendPlay(target, NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                    IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                    playerNetwork.sendMessage(new EmotePacket.Builder(data), null);
                }
            } catch (IOException e) {
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, ServerPlayer sourcePlayer, UUID target) {
        try {
            var targetPlayer = getPlayerFromUUID(target);
            if (PlayerTrackUtils.isTrackedBy(sourcePlayer, targetPlayer)){
                getPlayerNetworkInstance(targetPlayer).sendMessage(new EmotePacket.Builder(data), null);
            }

        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
        }
    }

    @Override
    protected void sendForPlayer(NetData data, ServerPlayer ignore, UUID target) {
        try {
            IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);

            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
            playerNetwork.sendMessage(packetBuilder, null);
        } catch (IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to send packet!", e);
        }
    }

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    public static CommonServerNetworkHandler getInstance() {
        return (CommonServerNetworkHandler) AbstractServerEmotePlay.getInstance();
    }
}
