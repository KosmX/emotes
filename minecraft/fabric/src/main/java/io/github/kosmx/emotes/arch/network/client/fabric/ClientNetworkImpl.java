package io.github.kosmx.emotes.arch.network.client.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
public class ClientNetworkImpl {
    public static boolean isServerChannelOpen(Identifier id) {
        return ClientPlayNetworking.canSend(id);
    }

    public static void sendPlayPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
