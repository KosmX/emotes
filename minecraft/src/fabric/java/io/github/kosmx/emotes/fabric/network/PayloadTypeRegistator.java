package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class PayloadTypeRegistator {
    public static void init() {
        register(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER_S2C, EmotePacketPayload.EMOTE_CHANNEL_READER_C2S);
        register(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER_S2C, EmotePacketPayload.STREAM_CHANNEL_READER_C2S);
    }

    private static void register(CustomPacketPayload.Type<EmotePacketPayload> type, StreamCodec<FriendlyByteBuf, EmotePacketPayload> s2c, StreamCodec<FriendlyByteBuf, EmotePacketPayload> c2s) {
        PayloadTypeRegistry.clientboundConfiguration().register(type, s2c);
        PayloadTypeRegistry.serverboundConfiguration().register(type, c2s);

        PayloadTypeRegistry.clientboundPlay().register(type, s2c);
        PayloadTypeRegistry.serverboundPlay().register(type, c2s);
    }
}
