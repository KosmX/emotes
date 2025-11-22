package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<AbstractServerNetwork> {
    private final Map<UUID, AvatarServerPlayNetwork> nonPlayers = new WeakHashMap<>();

    public void receiveMessage(EmotePacket packet, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(packet, getHandler(serverPlayer.connection));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ModdedServerPlayNetwork getHandler(ServerGamePacketListenerImpl handler) {
        return ((EmotesMixinNetwork) handler).emotecraft$getServerNetworkInstance();
    }

    public void receiveStreamMessage(EmotePacket packet, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            receiveStreamMessage(serverPlayer, getHandler(serverPlayer.connection), packet);
        }
    }

    @SuppressWarnings("unused")
    public void receiveStreamMessage(ServerPlayer player, ModdedServerPlayNetwork handler, EmotePacket packet) {
        player.connection.disconnect(Component.literal("This server does not support streaming!"));
    }

    @Override
    protected UUID getUUIDFromPlayer(AbstractServerNetwork player) {
        return player.getAvatar().getUUID();
    }

    @Override
    protected AbstractServerNetwork getPlayerFromUUID(UUID player) {
        ServerPlayer serverPlayer = NetworkPlatformTools.getServer().getPlayerList().getPlayer(player);
        if (serverPlayer != null) return getPlayerNetworkInstance(serverPlayer);

        if (!this.nonPlayers.containsKey(player)) {
            for (ServerLevel level : NetworkPlatformTools.getServer().getAllLevels()) {
                Entity entity = level.getEntity(player);
                if (entity instanceof Avatar avatar) {
                    this.nonPlayers.put(player, new AvatarServerPlayNetwork(avatar));
                    break;
                }
            }
        }
        return this.nonPlayers.get(player);
    }

    public AbstractServerNetwork getPlayerNetworkInstance(ServerPlayer player) {
        return ((EmotesMixinNetwork) player.connection).emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, AbstractServerNetwork player) {
        for (ServerPlayer target : NetworkPlatformTools.getTrackedBy(player.getAvatar())) {
            AbstractServerNetwork targetInstance = getPlayerNetworkInstance(target);
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
