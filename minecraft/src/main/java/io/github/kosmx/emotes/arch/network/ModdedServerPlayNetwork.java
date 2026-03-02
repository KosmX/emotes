package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.common.network.EmotePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for Emotes play network implementation
 */
public final class ModdedServerPlayNetwork extends AbstractServerNetwork {
    @NotNull
    private final ServerGamePacketListenerImpl serverGamePacketListener;

    public ModdedServerPlayNetwork(@NotNull ServerGamePacketListenerImpl serverGamePacketListener) {
        super();
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    protected @NotNull EmotesMixinConnection getServerConnection() {
        return (EmotesMixinConnection) ((ServerCommonPacketListenerAccessor)serverGamePacketListener).getConnection();
    }

    @Override
    protected @NotNull Avatar getAvatar() {
        return this.serverGamePacketListener.player;
    }

    @Override
    public boolean isActive() {
        return true; // TODO
    }

    @Override
    public void sendPlayMessage(EmotePacket packet) {
        this.serverGamePacketListener.send(NetworkPlatformTools.playPacket(packet));
    }
}
