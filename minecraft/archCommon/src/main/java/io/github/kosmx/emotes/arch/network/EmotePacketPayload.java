package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
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

    @NotNull
    public static StreamCodec<FriendlyByteBuf, EmotePacketPayload> reader(@NotNull CustomPacketPayload.Type<?> channel) {
        return CustomPacketPayload.codec((payload, buf) -> payload.packet().write(buf, buf.alloc()), buf -> new EmotePacketPayload(channel, new EmotePacket(buf)));
    }

    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> EMOTE_CHANNEL_READER = reader(NetworkPlatformTools.EMOTE_CHANNEL_ID);
    public static final StreamCodec<FriendlyByteBuf, EmotePacketPayload> STREAM_CHANNEL_READER = reader(NetworkPlatformTools.STREAM_CHANNEL_ID);
}
