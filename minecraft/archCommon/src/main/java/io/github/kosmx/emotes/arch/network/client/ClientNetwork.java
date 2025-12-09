package io.github.kosmx.emotes.arch.network.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
        sendMessage(writer, null);

        if (writer.data.emoteData != null && writer.data.emoteData.data().has("song") && writer.data.skippedPackets.contains(PacketConfig.NBS_CONFIG)) {
            PlatformTools.addToast(Component.translatable("emotecraft.song_too_big_to_send"));
        }
    }

    @Override
    public void sendMessage(EmotePacket byteBuffer, @Nullable UUID target) {
        sendPlayPacket(EmotePacketPayload.playPacket(byteBuffer));
    }

    @ExpectPlatform
    @Contract
    public static boolean isServerChannelOpen(Identifier id) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Contract
    public static void sendPlayPacket(CustomPacketPayload packet) {
        throw new AssertionError();
    }

    /**
     *
     * @param packet received data
     * @param configPacketConsumer if config phase, packet consumer
     */
    @SuppressWarnings("unused")
    public void receiveStreamMessage(@NotNull EmotePacket packet, @Nullable Consumer<CustomPacketPayload> configPacketConsumer) throws IOException {
        CommonData.LOGGER.error("Streaming message received!"); // TODO
    }

    public void receiveConfigMessage(@NotNull EmotePacket packet, @NotNull Consumer<CustomPacketPayload> consumer) throws IOException {
        if (packet.data.purpose == PacketTask.CONFIG) {
            setVersions(packet.data.versions);
            sendC2SConfig(p -> consumer.accept(EmotePacketPayload.playPacket(p.build())));
            this.isConfiguredNormally = true;
        } else if (packet.data.purpose == PacketTask.FILE) {
            EmoteHolder.addEmoteToList(packet.data.emoteData, this);
        } else {
            CommonData.LOGGER.warn("Invalid emotes packet type in configuration phase: " + packet.data.purpose);
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

            sendC2SConfig(p -> consumer.accept(EmotePacketPayload.playPacket(p.build())));
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
