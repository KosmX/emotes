package io.github.kosmx.emotes.neoforge.network;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public record EmotePacketPayload(@NotNull ResourceLocation id, @NotNull ByteBuffer bytes) implements CustomPacketPayload {
    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(bytes);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    public static @NotNull CustomPacketPayload createPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer bytes) {
        return new EmotePacketPayload(id, bytes);
    }

    public static @NotNull CustomPacketPayload playPacket(@NotNull ByteBuffer bytes) {
        return createPacket(NetworkPlatformTools.EMOTE_CHANNEL_ID, bytes);
    }

    public static @NotNull CustomPacketPayload streamPacket(@NotNull ByteBuffer bytes) {
        return createPacket(NetworkPlatformTools.STREAM_CHANNEL_ID, bytes);
    }


    public static @NotNull CustomPacketPayload geyserPacket(@NotNull ByteBuffer bytes) {
        return createPacket(new ResourceLocation("geyser", "emote"), bytes);
    }

    @NotNull
    public static FriendlyByteBuf.Reader<EmotePacketPayload> reader(@NotNull ResourceLocation channel) {
        return buf -> new EmotePacketPayload(channel, ByteBuffer.wrap(PlatformTools.unwrap(buf)));
    }

    public static final FriendlyByteBuf.Reader<EmotePacketPayload> EMOTE_CHANNEL_READER = reader(NetworkPlatformTools.EMOTE_CHANNEL_ID);
    public static final FriendlyByteBuf.Reader<EmotePacketPayload> STREAM_CHANNEL_READER = reader(NetworkPlatformTools.STREAM_CHANNEL_ID);
    public static final FriendlyByteBuf.Reader<EmotePacketPayload> GEYSER_CHANNEL_READER = reader(NetworkPlatformTools.GEYSER_CHANNEL_ID);
}
