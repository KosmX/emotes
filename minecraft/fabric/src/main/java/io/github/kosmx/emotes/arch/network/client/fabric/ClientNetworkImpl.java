package io.github.kosmx.emotes.arch.network.client.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class ClientNetworkImpl {
    public static boolean isServerChannelOpen(ResourceLocation id) {
        return ClientPlayNetworking.canSend(id);
    }

    @NotNull
    public static Packet<?> createServerboundPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer buf) {
        assert buf.hasRemaining();

        return ClientPlayNetworking.createC2SPacket(id, new FriendlyByteBuf(Unpooled.wrappedBuffer(buf)));
    }

}
