package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworkInstance {
    @SuppressWarnings("deprecation")
    public static void init() {
        // Configuration

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) ->
                NetworkPlatformTools.tryReceive(() ->
                        ClientNetwork.INSTANCE.receiveConfigMessage(buf.packet(), context.responseSender()::sendPacket))
        );

        ClientConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                NetworkPlatformTools.tryReceive(() ->
                        ClientNetwork.INSTANCE.receiveStreamMessage(buf.packet(), context.responseSender()::sendPacket))
        );

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

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                NetworkPlatformTools.tryReceive(() ->
                        ClientNetwork.INSTANCE.receiveStreamMessage(buf.packet(), context.responseSender()::sendPacket))
        );
    }
}
