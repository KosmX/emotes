package io.github.kosmx.emotes.forge.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ForgeGeyserEmotePacket(ByteBuf byteBuf) implements CustomPacketPayload {
    public static final ResourceLocation geyserChannelID = new ResourceLocation("geyser", "emote");

    @Override
    public void write(final FriendlyByteBuf buffer0) {
        buffer0.writeBytes(byteBuf);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return geyserChannelID;
    }
}