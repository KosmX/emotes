package io.github.kosmx.emotes.arch.network.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class NetworkPlatformToolsImpl {
    public static boolean canSendPlay(ServerPlayer player, ResourceLocation channel) {
        return ServerPlayNetworking.canSend(player, channel);
    }

    public static boolean canSendConfig(ServerConfigurationPacketListenerImpl player, ResourceLocation channel) {
        return ServerConfigurationNetworking.canSend(player, channel);
    }

    @NotNull
    public static Packet<?> createClientboundPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer buf) {
        assert buf.hasRemaining();
        return ServerPlayNetworking.createS2CPacket(id, new FriendlyByteBuf(Unpooled.wrappedBuffer(buf)));
    }
}
