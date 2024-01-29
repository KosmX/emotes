package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

/**
 * Wrapper class for Emotes play network implementation
 */
public class ModdedServerPlayNetwork implements IServerNetworkInstance {
    private final ServerGamePacketListenerImpl serverGamePacketListener;

    public ModdedServerPlayNetwork(ServerGamePacketListenerImpl serverGamePacketListener) {
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return serverGamePacketListener.emotecraft$getConnection().emotecraft$getRemoteVersions();
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        serverGamePacketListener.emotecraft$getConnection().emotecraft$setVersions(map);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        sendPlayMessage(builder.setVersion(getRemoteVersions()).build().write());
    }

    public void sendPlayMessage(ByteBuffer bytes) {
        serverGamePacketListener.send(new ClientboundCustomPayloadPacket(EmotePacketPayload.playPacket(bytes)));
    }


    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public int getRemoteVersion() {
        return 0;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return false;
    }

    @Override
    public int maxDataSize() {
        return 0;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return serverGamePacketListener.emotecraft$getEmoteTracker();
    }
}
