package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Wrapper class for Emotes play network implementation
 */
public class ModdedServerPlayNetwork extends AbstractServerNetwork implements IServerNetworkInstance {
    @NotNull
    protected final ServerGamePacketListenerImpl serverGamePacketListener;
    @NotNull
    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    public ModdedServerPlayNetwork(@NotNull ServerGamePacketListenerImpl serverGamePacketListener) {
        super();
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    protected @NotNull EmotesMixinConnection getServerConnection() {
        return (EmotesMixinConnection) ((ServerCommonPacketListenerAccessor)serverGamePacketListener).getConnection();
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        sendPlayMessage(builder.setVersion(getRemoteVersions()).build().write());
    }

    public void sendPlayMessage(ByteBuffer bytes) {
        this.serverGamePacketListener.send(NetworkPlatformTools.playPacket(bytes));
    }

    @Override
    public boolean isActive() {
        return true; // TODO
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return emotePlayTracker;
    }
}
