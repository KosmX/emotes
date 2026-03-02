package io.github.kosmx.emotes.arch.network.client.fabric;

import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
public final class ClientNetworkImpl extends ClientNetwork {
    @Override
    public boolean isServerChannelOpen(Identifier id) {
        return ClientPlayNetworking.canSend(id);
    }

    @Override
    public void sendPlayPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public boolean isActive() {
        try {
            return super.isActive();
        } catch (Exception ex) {
            return false;
        }
    }
}
