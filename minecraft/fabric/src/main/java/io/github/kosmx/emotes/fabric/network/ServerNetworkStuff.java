package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.ConfigTask;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

public final class ServerNetworkStuff {
    public static void init() {

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.EMOTE_CHANNEL_ID) &&
            ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.STREAM_CHANNEL_ID)) {
                handler.addTask(new ConfigTask(handler));
            }
            // No disconnect, vanilla clients can connect
        });



    }
}
