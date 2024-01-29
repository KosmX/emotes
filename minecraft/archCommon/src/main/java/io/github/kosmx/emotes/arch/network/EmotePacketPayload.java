package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.CommonData;
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

    public static EmotePacketPayload playPacket(ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID), bytes);
    }

    public static EmotePacketPayload streamPacket(ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation(CommonData.MOD_ID, CommonData.emoteStreamID), bytes);
    }


    public static EmotePacketPayload geyserPacket(ByteBuffer bytes) {
        return new EmotePacketPayload(new ResourceLocation("geyser", "emote"), bytes);
    }
}
