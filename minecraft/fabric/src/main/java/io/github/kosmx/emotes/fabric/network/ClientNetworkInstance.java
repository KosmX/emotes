package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.io.IOException;

public class ClientNetworkInstance {
    @SuppressWarnings("deprecation")
    public static void init() {
        // Configuration

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveConfigMessage(buf.packet(), context.responseSender()::sendPacket);
            } catch (IOException e) {
                CommonData.LOGGER.error("", e);
            }
        });

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveStreamMessage(buf.packet(), context.responseSender()::sendPacket);
            } catch (IOException e) {
                CommonData.LOGGER.error("", e);
            }
        });

        // Play
        C2SPlayChannelEvents.REGISTER.register((handler, sender, minecraft, channels) -> {
            if (channels.contains(NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                ClientNetwork.INSTANCE.configureOnPlay(sender::sendPacket);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNetwork.INSTANCE.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID,
                (buf, context) -> ClientNetwork.INSTANCE.receiveMessage(buf.packet())
        );

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) -> {
            try {
                ClientNetwork.INSTANCE.receiveStreamMessage(buf.packet(), context.responseSender()::sendPacket);
            } catch (IOException e) {
                CommonData.LOGGER.error("", e);
            }
        });
    }
}
