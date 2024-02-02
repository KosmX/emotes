package io.github.kosmx.emotes.arch.network.client.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ClientNetworkImpl {
    public static boolean isServerChannelOpen(ResourceLocation id) {
        return ClientPlayNetworking.canSend(id);
    }

}
