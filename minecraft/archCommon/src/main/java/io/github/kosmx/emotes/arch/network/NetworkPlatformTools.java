package io.github.kosmx.emotes.arch.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Collection;

public final class NetworkPlatformTools {
    public static final CustomPacketPayload.Type<EmotePacketPayload> EMOTE_CHANNEL_ID = new CustomPacketPayload.Type<>(McUtils.newIdentifier(CommonData.playEmoteID));
    public static final CustomPacketPayload.Type<EmotePacketPayload> STREAM_CHANNEL_ID = new CustomPacketPayload.Type<>(McUtils.newIdentifier(CommonData.emoteStreamID));

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
    @Contract
    public static Collection<ServerPlayer> getTrackedBy(ServerPlayer player) {
        throw new AssertionError();
    }

    public static @NotNull Packet<?> createClientboundPacket(@NotNull CustomPacketPayload.Type<?> id, @NotNull ByteBuffer buf) {
        assert (buf.hasRemaining()); // don't send empty packets

        return new ClientboundCustomPayloadPacket(new EmotePacketPayload(id, buf));
    }

    @ExpectPlatform
    public static MinecraftServer getServer() {
        throw new AssertionError();
    }

    public static @NotNull Packet<?> playPacket(@NotNull ByteBuffer buf) {
        return createClientboundPacket(EMOTE_CHANNEL_ID, buf);
    }

    public static @NotNull Packet<?> streamPacket(@NotNull ByteBuffer buf) {
        return createClientboundPacket(STREAM_CHANNEL_ID, buf);
    }
}
