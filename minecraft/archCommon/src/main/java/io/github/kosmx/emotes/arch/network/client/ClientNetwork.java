package io.github.kosmx.emotes.arch.network.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Don't forget to fire events:
 * - on player disconnect
 * - receive message (3x for 3 channels)
 * - handle configuration
 */
public final class ClientNetwork extends AbstractNetworkInstance {
    public static ClientNetwork INSTANCE = new ClientNetwork();

    @NotNull
    private final EmoteStreamHelper streamHelper = new EmoteStreamHelper() {
        @Override
        protected int getMaxPacketSize() {
            return maxDataSize();
        }

        @Override
        protected void sendPlayPacket(ByteBuffer buffer) {
            ClientNetwork.sendPlayPacket(EmotePacketPayload.playPacket(buffer));
        }

        @Override
        protected void sendStreamChunk(ByteBuffer buffer) {
            ClientNetwork.sendPlayPacket(EmotePacketPayload.streamPacket(buffer));
        }
    };

    @Override
    public boolean isActive() {
        return isServerChannelOpen(NetworkPlatformTools.EMOTE_CHANNEL_ID);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if (target != null) {
            builder.configureTarget(target);
        }

        var bytes = builder.build().write();

    }

    @Override
    protected void sendMessage(byte[] bytes, @Nullable UUID target) {
        sendMessage(ByteBuffer.wrap(bytes), null);
    }

    @Override
    protected void sendMessage(ByteBuffer byteBuffer, @Nullable UUID target) {
        sendPlayPacket(EmotePacketPayload.playPacket(byteBuffer));
    }

    @ExpectPlatform
    public static boolean isServerChannelOpen(ResourceLocation id) {
        throw new AssertionError();
    }


    public static void sendPlayPacket(CustomPacketPayload packet) {
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(new ServerboundCustomPayloadPacket(packet));
    }

    public void receiveMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf) {
        receiveMessage(PlatformTools.unwrap(buf)); // This will invoke EmotesProxy and handle the message
    }

    public void receiveStreamMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, boolean config) throws IOException {
        @Nullable ByteBuffer buffer = streamHelper.receiveStream(ByteBuffer.wrap(PlatformTools.unwrap(buf)));
        if (buffer != null) {
            if (config) {
                receiveConfigMessage(client, handler, buf);
            } else {
                receiveMessage(buffer, null);
            }
        }
    }

    public void receiveConfigMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf) throws IOException {
        var packet = new EmotePacket.Builder().build().read(ByteBuffer.wrap(PlatformTools.unwrap(buf)));
        if (packet != null) {
            if (packet.purpose == PacketTask.CONFIG) {
                setVersions(packet.versions);
            } else if (packet.purpose == PacketTask.FILE) {
                EmoteHolder.addEmoteToList(packet.emoteData).fromInstance = this;
            } else {
                if (EmoteInstance.config.showDebug.get()) {
                    EmoteInstance.instance.getLogger().log(Level.INFO, "Invalid emotes packet type in configuration phase: " + packet.purpose);
                }
            }
        } else {
            throw new IOException("Invalid emotes packet received in config phase");
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE - 16; // channel ID is 12, one extra int makes it 16 (string)
        // this way we have 3 byte error
    }
}
