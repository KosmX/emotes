package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public final class ServerNetworkStuff {
    public static void init() {
        PayloadTypeRegistator.init();

        // Config networking

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
                handler.addTask(new ConfigTask());
            } else { // No disconnect, vanilla clients can connect
                CommonData.LOGGER.debug("Client doesn't support emotes, ignoring");
            }
        });

        ServerConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (payload, context) -> {
            try {
                var message = payload.packet().data;
                if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");

                ((EmotesMixinConnection) ((ServerCommonPacketListenerAccessor) context.networkHandler()).getConnection()).emotecraft$setVersions(message.versions);
                UniversalEmoteSerializer.preparePackets(message.versions)
                        .map(EmotePacketPayload::playPacket)
                        .forEach(context.responseSender()::sendPacket);

                context.networkHandler().completeTask(ConfigTask.TYPE); // And, we're done here
            } catch (IOException e) {
                CommonData.LOGGER.error("", e);
                context.networkHandler().disconnect(Component.literal(CommonData.MOD_ID + ": " + e.getMessage()));
            }
        });

        // Play networking
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) ->
                CommonServerNetworkHandler.getInstance().receiveMessage(buf.packet(), context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                CommonServerNetworkHandler.getInstance().receiveStreamMessage(buf.packet(), context.player())
        );
    }
}
