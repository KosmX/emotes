package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.logging.Level;

public final class ServerNetworkStuff {
    public static void init() {
        PayloadTypeRegistator.init();

        // Config networking

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {

            if (ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.EMOTE_CHANNEL_ID) &&
                    ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.STREAM_CHANNEL_ID)) {

                handler.addTask(new ConfigTask());
            } else {
                LoggerService.INSTANCE.log(Level.FINE, "Client doesn't support emotes, ignoring");
            }
            // No disconnect, vanilla clients can connect
        });

        ServerConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) -> {
            try {
                var message = new EmotePacket.Builder().build().read(buf.bytes());
                if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");

                ((EmotesMixinConnection) ((ServerCommonPacketListenerAccessor) context.networkHandler()).getConnection()).emotecraft$setVersions(message.versions);
                UniversalEmoteSerializer.preparePackets(message.versions).forEach(buffer ->
                        context.responseSender().sendPacket(NetworkPlatformTools.playPacket(buffer))
                );
                context.networkHandler().completeTask(ConfigTask.TYPE); // And, we're done here
            } catch (IOException e) {
                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                context.networkHandler().disconnect(Component.literal(CommonData.MOD_ID + ": " + e.getMessage()));
            }
        });

        // Play networking
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (buf, context) ->
                CommonServerNetworkHandler.getInstance().receiveMessage(buf.unwrapBytes(), context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (buf, context) ->
                CommonServerNetworkHandler.getInstance().receiveStreamMessage(buf.unwrapBytes(), context.player())
        );
    }
}
