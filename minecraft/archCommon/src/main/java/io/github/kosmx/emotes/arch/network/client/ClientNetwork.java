package io.github.kosmx.emotes.arch.network.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Don't forget to fire events:
 * - on player disconnect
 * - receive message (3x for 3 channels)
 * - handle configuration
 */
public final class ClientNetwork extends AbstractNetworkInstance {
    public static ClientNetwork INSTANCE = new ClientNetwork();

    private boolean isConfiguredNormally;

    @Override
    public boolean isActive() {
        return isServerChannelOpen(NetworkPlatformTools.EMOTE_CHANNEL_ID.id());
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if (target != null) builder.configureTarget(target);

        var writer = builder.build();
        sendMessage(writer.write(), null);

        if (writer.data.emoteData != null && writer.data.emoteData.data().has("song") && !writer.data.writeSong) {
            PlatformTools.addToast(Component.translatable("emotecraft.song_too_big_to_send"));
        }
    }

    @Override
    protected void sendMessage(byte[] bytes, @Nullable UUID target) {
        sendMessage(ByteBuffer.wrap(bytes), null);
    }

    @Override
    public void sendMessage(ByteBuffer byteBuffer, @Nullable UUID target) {
        sendPlayPacket(EmotePacketPayload.playPacket(byteBuffer));
        if (byteBuffer.remaining() >= maxDataSize()) {
            CommonData.LOGGER.error("Sent packet size is {} byte(s)!", byteBuffer.remaining());
        }
    }

    @ExpectPlatform
    @Contract
    public static boolean isServerChannelOpen(ResourceLocation id) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Contract
    public static void sendPlayPacket(CustomPacketPayload packet) {
        throw new AssertionError();
    }

    /**
     *
     * @param buff received data
     * @param configPacketConsumer if config phase, packet consumer
     */
    @SuppressWarnings("unused")
    public void receiveStreamMessage(@NotNull ByteBuffer buff, @Nullable Consumer<CustomPacketPayload> configPacketConsumer) throws IOException {
        CommonData.LOGGER.error("Streaming message received!"); // TODO
    }

    public void receiveConfigMessage(@NotNull ByteBuffer buf, @NotNull Consumer<CustomPacketPayload> consumer) throws IOException {
        var packet = new EmotePacket.Builder().build().read(buf);
        if (packet.purpose == PacketTask.CONFIG) {
            setVersions(packet.versions);
            sendC2SConfig(p -> {
                try {
                    consumer.accept(EmotePacketPayload.playPacket(p.build().write()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            this.isConfiguredNormally = true;
        } else if (packet.purpose == PacketTask.FILE) {
            EmoteHolder.addEmoteToList(packet.emoteData, this);
        } else {
            CommonData.LOGGER.warn("Invalid emotes packet type in configuration phase: " + packet.purpose);
        }
    }

    /**
     * Used if the server has an outdated emotecraft that does not support the correct configuration
     * @deprecated Don't play on such servers
     */
    @Deprecated
    public void configureOnPlay(@NotNull Consumer<CustomPacketPayload> consumer) {
        if (!this.isConfiguredNormally && isActive()) {
            CommonData.LOGGER.warn("The server failed to configure the client, attempting to configure...");

            sendC2SConfig(p -> {
                try {
                    consumer.accept(EmotePacketPayload.playPacket(p.build().write()));
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
        return super.maxDataSize() - 16; // channel ID is 12, one extra int makes it 16 (string)
    }
}
