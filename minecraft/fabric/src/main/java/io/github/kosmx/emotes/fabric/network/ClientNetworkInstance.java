package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.fabric.FabricWrapper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.io.IOException;
import java.util.logging.Level;

public class ClientNetworkInstance {

    private static final ClientNetwork network = ClientNetwork.INSTANCE;

    public static void init(){

        // Configuration

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveConfigMessage(buf, responseSender::sendPacket);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveStreamMessage(buf, responseSender::sendPacket);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });

        // Play

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> network.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (client, handler, buf, responseSender) -> network.receiveMessage(buf));

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (client, handler, buf, responseSender) -> {
            try {
                network.receiveStreamMessage(buf, null);
            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        });

    }

}
