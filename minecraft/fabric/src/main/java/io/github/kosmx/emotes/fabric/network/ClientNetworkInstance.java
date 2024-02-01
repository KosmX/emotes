package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.io.IOException;
import java.util.logging.Level;

public class ClientNetworkInstance {


    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();
    private final ClientNetwork network = ClientNetwork.INSTANCE;

    public void init(){

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> network.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (client, handler, buf, responseSender) -> network.receiveMessage(buf));

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveStreamMessage(buf, false);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });

        // Configuration

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveConfigMessage(buf);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveStreamMessage(buf, true);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });
    }

}
