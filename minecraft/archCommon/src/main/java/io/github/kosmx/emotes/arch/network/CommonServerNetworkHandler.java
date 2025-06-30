package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<ModdedServerPlayNetwork> {
    public void receiveMessage(byte[] bytes, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(bytes, getHandler(serverPlayer.connection));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ModdedServerPlayNetwork getHandler(ServerGamePacketListenerImpl handler) {
        return ((EmotesMixinNetwork) handler).emotecraft$getServerNetworkInstance();
    }

    public void receiveStreamMessage(byte[] bytes, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            receiveStreamMessage(serverPlayer, getHandler(serverPlayer.connection), ByteBuffer.wrap(bytes));
        }
    }

    public void receiveStreamMessage(ServerPlayer player, ModdedServerPlayNetwork handler, ByteBuffer buf) {
        try {
            if (((EmotesMixinNetwork)handler).emotecraft$getServerNetworkInstance().allowEmoteStreaming()) {
                var packet = handler.receiveStreamChunk(buf);
                if (packet != null) {
                    receiveMessage(packet.array(), handler);
                }
            } else {
                player.connection.disconnect(Component.literal("Emote stream is disabled on this server"));
            }
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to receive packet!", e);
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(ModdedServerPlayNetwork player) {
        return player.serverGamePacketListener.getPlayer().getUUID();
    }

    @Override
    protected ModdedServerPlayNetwork getPlayerFromUUID(UUID player) {
        return getPlayerNetworkInstance(NetworkPlatformTools.getServer().getPlayerList().getPlayer(player));
    }

    public ModdedServerPlayNetwork getPlayerNetworkInstance(ServerPlayer player) {
        return ((EmotesMixinNetwork) player.connection).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, ModdedServerPlayNetwork player) {
        for (ServerPlayer target : PlayerTrackUtils.getTrackedBy(player.serverGamePacketListener.getPlayer())) {
            ModdedServerPlayNetwork targetInstance = getPlayerNetworkInstance(target);
            if (targetInstance == player) continue;

            if (NetworkPlatformTools.canSendPlay(target, NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                sendForPlayer(data, player, targetInstance);
            }
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
