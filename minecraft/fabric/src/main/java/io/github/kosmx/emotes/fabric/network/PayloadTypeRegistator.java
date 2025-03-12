package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class PayloadTypeRegistator {
    public static void init() {
        register(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER);
        register(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER);
    }

    private static void register(CustomPacketPayload.Type<EmotePacketPayload> type, StreamCodec<FriendlyByteBuf, EmotePacketPayload> codec) {
        PayloadTypeRegistry.configurationS2C().register(type, codec);
        PayloadTypeRegistry.configurationC2S().register(type, codec);

        PayloadTypeRegistry.playS2C().register(type, codec);
        PayloadTypeRegistry.playC2S().register(type, codec);
    }
}
