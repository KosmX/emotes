package io.github.kosmx.emotes.arch.network.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
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
            ClientNetwork.sendPlayPacket(playPacket(buffer));
        }

        @Override
        protected void sendStreamChunk(ByteBuffer buffer) {
            ClientNetwork.sendPlayPacket(streamPacket(buffer));
        }
    };

    private boolean isConfiguredNormally;

    @Override
    public boolean isActive() {
        return isServerChannelOpen(NetworkPlatformTools.EMOTE_CHANNEL_ID.id());
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if (target != null) {
            builder.configureTarget(target);
        }

        var writer = builder.build();
        var bytes = writer.write();
        sendMessage(bytes, null);

        if(writer.data.emoteData != null && writer.data.emoteData.extraData.containsKey("song") && !writer.data.writeSong){
            TmpGetters.getClientMethods().sendChatMessage(Component.translatable("emotecraft.song_too_big_to_send"));
        }

    }

    @Override
    protected void sendMessage(byte[] bytes, @Nullable UUID target) {
        sendMessage(ByteBuffer.wrap(bytes), null);
    }

    @Override
    public void sendMessage(ByteBuffer byteBuffer, @Nullable UUID target) {
        sendPlayPacket(playPacket(byteBuffer));
        EmoteInstance.instance.getLogger().log(Level.INFO, "Sent packet size is " + byteBuffer.remaining() + " byte(s).", false);
    }

    @ExpectPlatform
    @Contract
    public static boolean isServerChannelOpen(ResourceLocation id) {
        throw new AssertionError();
    }


    public static void sendPlayPacket(Packet<?> packet) {
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(packet);
    }

    /**
     *
     * @param buff received data
     * @param configPacketConsumer if config phase, packet consumer
     */
    public void receiveStreamMessage(@NotNull ByteBuffer buff, @Nullable Consumer<Packet<?>> configPacketConsumer) throws IOException {
        @Nullable ByteBuffer buffer = streamHelper.receiveStream(buff);
        if (buffer != null) {
            if (configPacketConsumer != null) {
                receiveConfigMessage(buffer, configPacketConsumer);
            } else {
                receiveMessage(buffer, null);
            }
        }
    }

    public void receiveConfigMessage(@NotNull ByteBuffer buf, @NotNull Consumer<Packet<?>> consumer) throws IOException {
        var packet = new EmotePacket.Builder().build().read(buf);
        if (packet != null) {
            if (packet.purpose == PacketTask.CONFIG) {
                setVersions(packet.versions);
                sendC2SConfig(p -> {
                    try {
                        consumer.accept(playPacket(p.build().write()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                this.isConfiguredNormally = true;
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

    /**
     * Used if the server has an outdated emotecraft that does not support the correct configuration
     * @deprecated Don't play on such servers
     */
    @Deprecated
    public void configureOnPlay(@NotNull Consumer<Packet<?>> consumer) {
        if (!this.isConfiguredNormally && isActive()) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "The server failed to configure the client, attempting to configure...");

            sendC2SConfig(p -> {
                try {
                    consumer.accept(ClientNetwork.playPacket(p.build().write()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        this.isConfiguredNormally = false;
    }

    @Override
    public int maxDataSize() {
        return CommonData.MAX_PACKET_SIZE - 16; // channel ID is 12, one extra int makes it 16 (string)
    }

    @ExpectPlatform
    public static @NotNull Packet<?> createServerboundPacket(@NotNull final CustomPacketPayload.Type<EmotePacketPayload> id, @NotNull ByteBuffer buf) {
        throw new AssertionError();
    }

    public static @NotNull Packet<?> playPacket(@NotNull ByteBuffer buf) {
        return createServerboundPacket(NetworkPlatformTools.EMOTE_CHANNEL_ID, buf);
    }

    public static @NotNull Packet<?> streamPacket(@NotNull ByteBuffer buf) {
        return createServerboundPacket(NetworkPlatformTools.STREAM_CHANNEL_ID, buf);
    }
    // no geyser packet from client. That is geyser plugin only feature
}
