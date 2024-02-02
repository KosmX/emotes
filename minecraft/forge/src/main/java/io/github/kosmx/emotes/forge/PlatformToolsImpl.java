package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;

public class PlatformToolsImpl {
    public static boolean isPlayerAnimLoaded() {
        try {
            Class.forName("dev.kosmx.playerAnim.api.layered.IAnimation").getName();
            return true;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }

    public static INetworkInstance getClientNetworkController() {
        return ClientNetwork.INSTANCE;
    }

}
