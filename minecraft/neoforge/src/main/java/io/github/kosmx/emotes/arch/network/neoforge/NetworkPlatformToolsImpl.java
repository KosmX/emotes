package io.github.kosmx.emotes.arch.network.neoforge;

import io.github.kosmx.emotes.neoforge.network.EmotePacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class NetworkPlatformToolsImpl {
    public static @NotNull Packet<?> createClientboundPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer buf) {

        assert (buf.hasRemaining()); // don't send empty packets

        return new ClientboundCustomPayloadPacket(new EmotePacketPayload(id, buf));
    }

    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        return player.connection.isConnected(channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl packetListener, ResourceLocation channel) {
        return packetListener.isConnected(channel);
    }
}
