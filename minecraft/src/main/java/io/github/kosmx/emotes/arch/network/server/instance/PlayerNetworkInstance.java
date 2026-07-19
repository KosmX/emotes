package io.github.kosmx.emotes.arch.network.server.instance;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Wrapper class for Emotes play network implementation
 */
public final class PlayerNetworkInstance extends McServerNetworkInstance {
    @Nullable
    private ServerGamePacketListenerImpl serverGamePacketListener;

    public void setServerGamePacketListener(@NotNull ServerGamePacketListenerImpl serverGamePacketListener) {
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    public @NotNull Avatar getAvatar() {
        return Objects.requireNonNull(this.serverGamePacketListener).player;
    }

    @Override
    public boolean isActive() {
        return this.serverGamePacketListener != null;
    }

    @Override
    public void sendPlayMessage(EmotePacket packet) {
        Objects.requireNonNull(this.serverGamePacketListener).send(NetworkPlatformTools.playPacket(packet));
    }

    @Override
    public void disconnect() {
        // no-op
    }

    public void receiveConfigMessage(EmotePacket packet, Consumer<Packet<?>> consumer) throws IOException {
        NetData message = packet.data;
        if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");
        setVersions(message.versions);

        UniversalEmoteSerializer.preparePackets()
                .map(builder -> {
                    builder.setVersion(getVersions());
                    return NetworkPlatformTools.playPacket(builder.build());
                })
                .forEach(consumer);
    }
}
