package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
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
    @NotNull private final ServerGamePacketListenerImpl serverGamePacketListener;

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
    void sendEmotePacket(ByteBuffer buffer) {
        sendPlayMessage(buffer);
    }


    @Override
    public void sendGeyserPacket(ByteBuffer buffer) {
        serverGamePacketListener.send(new ClientboundCustomPayloadPacket(EmotePacketPayload.geyserPacket(buffer)));
    }

    @Override
    public void disconnect(String literal) {
        serverGamePacketListener.disconnect(Component.literal(literal));
    }

    @Override
    void sendStreamPacket(ByteBuffer buffer) {
        serverGamePacketListener.send(new ClientboundCustomPayloadPacket(EmotePacketPayload.streamPacket(buffer)));
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        sendPlayMessage(builder.setVersion(getRemoteVersions()).build().write());
    }

    public void sendPlayMessage(ByteBuffer bytes) {
        serverGamePacketListener.send(new ClientboundCustomPayloadPacket(EmotePacketPayload.playPacket(bytes)));
    }

    public void sendPlayStream(ByteBuffer bytes) {
        if (getRemoteVersions().getOrDefault(PacketConfig.ALLOW_EMOTE_STREAM, (byte)1) != 0) {
            streamHelper.sendMessage(bytes);
        } else {
            sendPlayMessage(bytes);
        }
    }

    // TODO isActive

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return emotePlayTracker;
    }
}
