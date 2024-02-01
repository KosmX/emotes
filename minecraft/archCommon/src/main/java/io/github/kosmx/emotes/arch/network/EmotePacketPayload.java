package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.Function;

public record EmotePacketPayload(@NotNull ResourceLocation id, @NotNull ByteBuffer bytes) implements CustomPacketPayload {
    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(bytes);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    public static @NotNull EmotePacketPayload playPacket(@NotNull ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID), bytes);
    }

    public static @NotNull EmotePacketPayload streamPacket(@NotNull ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation(CommonData.MOD_ID, CommonData.emoteStreamID), bytes);
    }


    public static @NotNull EmotePacketPayload geyserPacket(@NotNull ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation("geyser", "emote"), bytes);
    }

    @NotNull
    public static FriendlyByteBuf.Reader<EmotePacketPayload> reader(@NotNull ResourceLocation channel) {
        return buf -> new EmotePacketPayload(channel, ByteBuffer.wrap(PlatformTools.unwrap(buf)));
    }

    public static final FriendlyByteBuf.Reader<EmotePacketPayload> EMOTE_CHANNEL_READER = reader(NetworkPlatformTools.EMOTE_CHANNEL_ID);
    public static final FriendlyByteBuf.Reader<EmotePacketPayload> STREAM_CHANNEL_READER = reader(NetworkPlatformTools.STREAM_CHANNEL_ID);
    public static final FriendlyByteBuf.Reader<EmotePacketPayload> GEYSER_CHANNEL_READER = reader(NetworkPlatformTools.GEYSER_CHANNEL_ID);
}
