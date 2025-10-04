package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

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
    public void sendPlayMessage(ByteBuffer bytes) {
        this.serverGamePacketListener.send(NetworkPlatformTools.playPacket(bytes));
    }
}
