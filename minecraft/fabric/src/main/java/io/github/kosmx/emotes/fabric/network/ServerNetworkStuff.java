package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

public final class ServerNetworkStuff {
    public static void init() {

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {

            if (ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.EMOTE_CHANNEL_ID) &&
                    ServerConfigurationNetworking.canSend(handler, NetworkPlatformTools.STREAM_CHANNEL_ID)) {

                handler.addTask(new ConfigTask());
            } else {
                EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
            }
            // No disconnect, vanilla clients can connect
        });

        ServerConfigurationNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (server, handler, buf, responseSender) -> {

            try {
                var message = new EmotePacket.Builder().build().read(ByteBuffer.wrap(PlatformTools.unwrap(buf)));

                if (message == null || message.purpose != PacketTask.CONFIG)
                    throw new IOException("Wrong packet type for config task");
                ((EmotesMixinConnection) ((ServerCommonPacketListenerAccessor) handler).getConnection()).emotecraft$setVersions(message.versions);
                CommonServerNetworkHandler.instance.getServerEmotes(message.versions).forEach(buffer ->
                        new EmoteStreamHelper() {

                            @Override
                            protected int getMaxPacketSize() {
                                return Short.MAX_VALUE - 16;
                            }

                            @Override
                            protected void sendPlayPacket(ByteBuffer buffer) {
                                responseSender.sendPacket(ClientNetwork.playPacket(buffer));
                            }

                            @Override
                            protected void sendStreamChunk(ByteBuffer buffer) {
                                responseSender.sendPacket(ClientNetwork.streamPacket(buffer));
                            }
                        }
                );

                handler.completeTask(ConfigTask.TYPE); // And, we're done here

            } catch (IOException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                handler.disconnect(Component.literal(CommonData.MOD_ID + ": " + e.getMessage()));
            }
        });

        // Play networking
        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.EMOTE_CHANNEL_ID, (server, player, handler, buf, responseSender) -> CommonServerNetworkHandler.instance.receiveMessage(player, handler, buf));

        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.STREAM_CHANNEL_ID, (server, player, handler, buf, responseSender) -> CommonServerNetworkHandler.instance.receiveStreamMessage(player, handler, buf));

        ServerPlayNetworking.registerGlobalReceiver(NetworkPlatformTools.GEYSER_CHANNEL_ID, (server, player, handler, buf, responseSender) -> CommonServerNetworkHandler.instance.receiveGeyserMessage(player, buf));
    }
}
