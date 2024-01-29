package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlatformToolsImpl {
    public static boolean isPlayerAnimLoaded() {
        return FabricLoader.getInstance().isModLoaded("player-animator");
    }

    public static INetworkInstance getClientNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }

}
