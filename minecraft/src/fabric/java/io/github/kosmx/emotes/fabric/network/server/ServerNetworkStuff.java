package io.github.kosmx.emotes.fabric.network.server;

import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.server.McServerEmotePlay;
import io.github.kosmx.emotes.arch.network.server.McConfigTask;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.fabric.network.PayloadTypeRegistator;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

public final class ServerNetworkStuff {
    public static void init() {
        PayloadTypeRegistator.init();

        // Config networking

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, _) -> {
            if (ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
                ((FabricServerConfigurationPacketListenerImpl)handler).addTask(new McConfigTask());
            } else { // No disconnect, vanilla clients can connect
                CommonData.LOGGER.debug("Client doesn't support emotes, ignoring");
            }
        });

        ServerConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (payload, context) -> {
            try {
                context.packetListener().connection.emotecraft$receiveConfigMessage(payload.packet(), context.responseSender()::sendPacket);
                ((FabricServerConfigurationPacketListenerImpl)context.packetListener()).completeTask(McConfigTask.TYPE); // And, we're done here
            } catch (Exception e) {
                CommonData.LOGGER.error("Invalid Emotecraft packet!", e);
                context.packetListener().disconnect(Component.literal(CommonData.MOD_ID + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())));
            }
        });

        // Play networking
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) ->
                McServerEmotePlay.getInstance().receiveMessage(buf.packet(), context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                McServerEmotePlay.getInstance().receiveStreamMessage(buf.packet(), context.player())
        );
    }
}
