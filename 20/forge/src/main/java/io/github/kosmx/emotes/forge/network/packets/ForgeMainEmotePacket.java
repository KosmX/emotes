package io.github.kosmx.emotes.forge.network.packets;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public record ForgeMainEmotePacket(ByteBuf byteBuf) implements CustomPacketPayload {
    public static final ResourceLocation channelID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);

    @Override
    public void write(final FriendlyByteBuf buffer0) {
        buffer0.writeBytes(byteBuf);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return channelID;
    }

    public static ForgeMainEmotePacket newEmotePacket(NetData data) throws IOException {
        return new ForgeMainEmotePacket(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array()));
    }
}
