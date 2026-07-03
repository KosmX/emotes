package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundPlayChannelEvents;
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
        ServerboundPlayChannelEvents.REGISTER.register((_, sender, _, channels) -> {
            if (channels.contains(NetworkPlatformTools.EMOTE_CHANNEL_ID.id())) {
                ClientNetwork.INSTANCE.configureOnPlay(sender::sendPacket);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientNetwork.INSTANCE.disconnect());

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID,
                (buf, _) -> ClientNetwork.INSTANCE.receiveMessage(buf.packet(), null)
        );

        ClientPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                NetworkPlatformTools.tryReceive(() ->
                        ClientNetwork.INSTANCE.receiveStreamMessage(buf.packet(), context.responseSender()::sendPacket))
        );
    }
}
