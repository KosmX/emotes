package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record EmotePacketPayload(@NotNull CustomPacketPayload.Type<?> id, @NotNull EmotePacket packet) implements CustomPacketPayload {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return id;
    }

    public static @NotNull CustomPacketPayload playPacket(@NotNull EmotePacket packet) {
        return new EmotePacketPayload(NetworkPlatformTools.EMOTE_CHANNEL_ID, packet);
    }

    public static @NotNull CustomPacketPayload streamPacket(@NotNull EmotePacket packet) {
        return new EmotePacketPayload(NetworkPlatformTools.STREAM_CHANNEL_ID, packet);
    }

    /**
     * A codec for a single physical direction. The {@code bound} (the packet's destination endpoint)
     * is used for BOTH encoding and decoding, so the writer and reader always filter the same
     * sub-packet set. S2C ⇒ {@link PacketBound#CLIENT}, C2S ⇒ {@link PacketBound#SERVER}.
     */
    @NotNull
    public static StreamCodec<FriendlyByteBuf, EmotePacketPayload> reader(@NotNull CustomPacketPayload.Type<?> channel, PacketBound bound) {
        return CustomPacketPayload.codec(
                (payload, buf) -> payload.packet().write(buf, bound),
                buf -> new EmotePacketPayload(channel, new EmotePacket(buf, bound))
        );
    }

    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> EMOTE_CHANNEL_READER_S2C = reader(
            NetworkPlatformTools.EMOTE_CHANNEL_ID, PacketBound.CLIENT
    );
    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> EMOTE_CHANNEL_READER_C2S = reader(
            NetworkPlatformTools.EMOTE_CHANNEL_ID, PacketBound.SERVER
    );

    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> STREAM_CHANNEL_READER_S2C = reader(
            NetworkPlatformTools.STREAM_CHANNEL_ID, PacketBound.CLIENT
    );
    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> STREAM_CHANNEL_READER_C2S = reader(
            NetworkPlatformTools.STREAM_CHANNEL_ID, PacketBound.SERVER
    );
}
