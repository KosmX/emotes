package io.github.kosmx.emotes.arch.network.client.neoforge;

import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.Objects;

public final class ClientNetworkImpl extends ClientNetwork {
    @Override
    public boolean isServerChannelOpen(Identifier id) {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).hasChannel(id);
    }

    @Override
    public void sendPlayPacket(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
