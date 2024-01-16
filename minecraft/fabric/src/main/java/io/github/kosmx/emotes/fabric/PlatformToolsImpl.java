package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import net.fabricmc.loader.api.FabricLoader;

public class PlatformToolsImpl {
    public static boolean isPlayerAnimLoaded() {
        return FabricLoader.getInstance().isModLoaded("player-animator");
    }

    public static INetworkInstance getClientNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }
}
