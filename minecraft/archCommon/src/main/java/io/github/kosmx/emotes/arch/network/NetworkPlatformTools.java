package io.github.kosmx.emotes.arch.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class NetworkPlatformTools {
    public static final ResourceLocation EMOTE_CHANNEL_ID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);
    public static final ResourceLocation STREAM_CHANNEL_ID = new ResourceLocation(CommonData.MOD_ID, CommonData.emoteStreamID);
    public static final ResourceLocation GEYSER_CHANNEL_ID = new ResourceLocation("geyser", "emote");


    @ExpectPlatform
    @Contract // contract to fix flow analysis.
    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Contract
    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl player, ResourceLocation channel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static @NotNull Packet<?> createClientboundPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer buf) {
        throw new AssertionError();
    }

    public static @NotNull Packet<?> playPacket(@NotNull ByteBuffer buf) {
        return createClientboundPacket(EMOTE_CHANNEL_ID, buf);
    }

    public static @NotNull Packet<?> streamPacket(@NotNull ByteBuffer buf) {
        return createClientboundPacket(EMOTE_CHANNEL_ID, buf);
    }

    public static @NotNull Packet<?> geyserPacket(@NotNull ByteBuffer buf) {
        return createClientboundPacket(EMOTE_CHANNEL_ID, buf);
    }
}
