package io.github.kosmx.emotes.arch.network.server.instance;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import org.jspecify.annotations.NonNull;

/**
 * Wrapper class for Emotes play network implementation
 */
public final class PlayerNetworkInstance extends McServerNetworkInstance {
    @NonNull
    private final ServerGamePacketListenerImpl serverGamePacketListener;

    public PlayerNetworkInstance(@NonNull ServerGamePacketListenerImpl serverGamePacketListener, @NonNull ConfigNetworkInstance config) {
        super(config);
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    public @NonNull Avatar getAvatar() {
        return this.serverGamePacketListener.player;
    }

    @Override
    public boolean isActive() {
        return NetworkPlatformTools.INSTANCE.canSendPlay(
                this.serverGamePacketListener.player, NetworkPlatformTools.EMOTE_CHANNEL_ID.id()
        );
    }

    @Override
    public void sendPlayMessage(EmotePacket packet) {
        if (!isActive()) return;
        this.serverGamePacketListener.send(NetworkPlatformTools.playPacket(packet));
    }

    @Override
    public void disconnect() {
        // no-op
    }
}
