package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.io.IOException;
import java.util.logging.Level;

public class ClientNetworkInstance {

    public static void init(){
        // Configuration

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveConfigMessage(buf.bytes(), context.responseSender()::sendPacket);
            } catch (IOException e) {
                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
            }
        });

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveStreamMessage(buf.bytes(), context.responseSender()::sendPacket);
            } catch (IOException e) {
                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
            }
        });

        // Play
        C2SPlayChannelEvents.REGISTER.register((handler, sender, minecraft, channels) -> {
            if (channels.contains(NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                ClientNetwork.INSTANCE.configureOnPlay(sender::sendPacket);
            }
        });
        // ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientNetwork.INSTANCE.configureOnPlay(sender::sendPacket));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNetwork.INSTANCE.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID,
                (buf, context) -> ClientNetwork.INSTANCE.receiveMessage(buf.unwrapBytes())
        );

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveStreamMessage(buf.bytes(), null);
            } catch (IOException e) {
                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
            }
        });
    }
}
