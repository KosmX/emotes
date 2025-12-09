package io.github.kosmx.emotes.arch.network.client.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.Objects;

@SuppressWarnings("unused")
public class ClientNetworkImpl {
    public static boolean isServerChannelOpen(Identifier id) {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).hasChannel(id);
    }

    public static void sendPlayPacket(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
