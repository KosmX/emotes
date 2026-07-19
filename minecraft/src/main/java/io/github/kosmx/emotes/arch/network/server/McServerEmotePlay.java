package io.github.kosmx.emotes.arch.network.server;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.server.instance.AvatarNetworkInstance;
import io.github.kosmx.emotes.arch.network.server.instance.McServerNetworkInstance;
import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class McServerEmotePlay extends AbstractServerEmotePlay<McServerNetworkInstance> {
    private final Map<UUID, AvatarNetworkInstance> nonPlayers = new WeakHashMap<>();

    public void receiveMessage(EmotePacket packet, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                receiveMessage(packet, serverPlayer.connection.emotecraft$getServerNetworkInstance());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void receiveStreamMessage(EmotePacket packet, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            receiveStreamMessage(serverPlayer, serverPlayer.connection.emotecraft$getServerNetworkInstance(), packet);
        }
    }

    @SuppressWarnings("unused")
    public void receiveStreamMessage(ServerPlayer player, PlayerNetworkInstance handler, EmotePacket packet) {
        player.connection.disconnect(Component.literal("This server does not support streaming!"));
    }

    @Override
    protected McServerNetworkInstance getPlayerFromUUID(UUID player) {
        ServerPlayer serverPlayer = NetworkPlatformTools.INSTANCE.getServer().getPlayerList().getPlayer(player);
        if (serverPlayer != null) return serverPlayer.connection.emotecraft$getServerNetworkInstance();

        if (!this.nonPlayers.containsKey(player)) {
            for (ServerLevel level : NetworkPlatformTools.INSTANCE.getServer().getAllLevels()) {
                Entity entity = level.getEntity(player);
                if (entity instanceof Avatar avatar) {
                    this.nonPlayers.put(player, new AvatarNetworkInstance(avatar));
                    break;
                }
            }
        }
        return this.nonPlayers.get(player);
    }

    @Override
    protected void sendForTrackedBy(NetData data, McServerNetworkInstance player) {
        for (ServerPlayer target : NetworkPlatformTools.INSTANCE.getTrackedBy(player.getAvatar())) {
            McServerNetworkInstance targetInstance = target.connection.emotecraft$getServerNetworkInstance();
            if (targetInstance == player) continue;

            if (NetworkPlatformTools.INSTANCE.canSendPlay(target, NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                targetInstance.sendMessage(data, true);
            }
        }
    }

    @Override
    protected void sendForEveryone(NetData data) {
        for (ServerPlayer player : NetworkPlatformTools.INSTANCE.getServer().getPlayerList().getPlayers()) {
            McServerNetworkInstance targetInstance = player.connection.emotecraft$getServerNetworkInstance();
            if (NetworkPlatformTools.INSTANCE.canSendPlay(player, NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                targetInstance.sendMessage(data, true);
            }
        }
    }

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    public static McServerEmotePlay getInstance() {
        return (McServerEmotePlay) AbstractServerEmotePlay.getInstance();
    }
}
