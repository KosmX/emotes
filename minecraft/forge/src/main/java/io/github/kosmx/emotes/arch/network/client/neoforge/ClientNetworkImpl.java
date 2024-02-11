package io.github.kosmx.emotes.arch.network.client.neoforge;

import io.github.kosmx.emotes.forge.network.EmotePacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ClientNetworkImpl {
    public static boolean isServerChannelOpen(ResourceLocation id) {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).isConnected(id);
    }

    @NotNull
    public static Packet<?> createServerboundPacket(@NotNull ResourceLocation id, @NotNull ByteBuffer buf) {
        assert buf.hasRemaining();

        return new ClientboundCustomPayloadPacket(EmotePacketPayload.createPacket(id, buf));
    }

}
