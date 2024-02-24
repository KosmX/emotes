package io.github.kosmx.emotes.neoforge.network;

import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.arch.mixin.ServerCommonPacketListenerAccessor;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.neoforge.fixingbadsoftware.UnNeoForgifierConfigurationTaskWrapper;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * Networking hell
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetwork {

    @SubscribeEvent
    public static void registerPlay(final RegisterPayloadHandlerEvent event) {

        // Play networking

        var emotes = event.registrar("emotecraft");
        emotes.optional().play(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER, handler -> {
            handler.client((arg, playPayloadContext) -> ClientNetwork.INSTANCE.receiveMessage(arg.bytes(), null));

            // Why can't forge simply create a networking module that doesn't suck?
            handler.server((arg, playPayloadContext) -> {
                var data = extractComponents(playPayloadContext);
                try {
                    CommonServerNetworkHandler.instance.receiveMessage(arg.bytes().array(), data.getRight(), data.getLeft());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        emotes.optional().play(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER, handler -> {
            handler.client((arg, playPayloadContext) -> {
                try {
                    ClientNetwork.INSTANCE.receiveStreamMessage(arg.bytes(), null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            handler.server((arg, playPayloadContext) -> {
                var data = extractComponents(playPayloadContext);
                CommonServerNetworkHandler.instance.receiveStreamMessage(data.getRight(), data.getLeft(), arg.bytes());
            });
        });


        event.registrar("geyser").optional().play(NetworkPlatformTools.GEYSER_CHANNEL_ID, EmotePacketPayload.GEYSER_CHANNEL_READER, handler -> {
            handler.server((arg, playPayloadContext) -> CommonServerNetworkHandler.instance.receiveGeyserMessage((ServerPlayer) playPayloadContext.player().get(), arg.bytes().array()));
        }); // I may hack forge later

        // Config networking

        emotes.optional().configuration(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER, handler -> {
            handler.server((arg, configurationPayloadContext) -> {

                try {
                    var message = new EmotePacket.Builder().build().read(arg.bytes());

                    if (message == null || message.purpose != PacketTask.CONFIG)
                        throw new IOException("Wrong packet type for config task");
                    // TODO get connection handler
                    connection.emotecraft$setVersions(message.versions);
                    CommonServerNetworkHandler.instance.getServerEmotes(message.versions).forEach(buffer ->
                            new EmoteStreamHelper() {

                                @Override
                                protected int getMaxPacketSize() {
                                    return Short.MAX_VALUE - 16;
                                }

                                @Override
                                protected void sendPlayPacket(ByteBuffer buffer) {
                                    configurationPayloadContext.replyHandler().send(EmotePacketPayload.playPacket(buffer));
                                }

                                @Override
                                protected void sendStreamChunk(ByteBuffer buffer) {
                                    configurationPayloadContext.replyHandler().send(EmotePacketPayload.streamPacket(buffer));
                                }
                            }
                    );

                    configurationPayloadContext.taskCompletedHandler().onTaskCompleted(ConfigTask.TYPE);

                } catch (IOException e) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                    configurationPayloadContext.channelHandlerContext().disconnect();
                }
            });
            handler.client((arg, configurationPayloadContext) -> {
                try {
                    ClientNetwork.INSTANCE.receiveConfigMessage(arg.bytes(), p -> configurationPayloadContext.replyHandler().send(((ServerboundCustomPayloadPacket)p).payload()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });


        emotes.optional().configuration(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER, handler -> {
            handler.client((arg, configurationPayloadContext) -> {
                try {
                    ClientNetwork.INSTANCE.receiveStreamMessage(arg.bytes(), p -> configurationPayloadContext.replyHandler().send(((ServerboundCustomPayloadPacket)p).payload()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    @SubscribeEvent
    public static void registerNetworkConfigTask(final OnGameConfigurationEvent event) {
        if (event.getListener().isConnected(NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
            event.register(UnNeoForgifierConfigurationTaskWrapper.wrap(new ConfigTask()));
        } else {
            EmoteInstance.instance.getLogger().log(Level.FINE, "Client doesn't support emotes, ignoring");
        }
    }


    private static Pair<IServerNetworkInstance, ServerPlayer> extractComponents(PlayPayloadContext haystack) {
        ServerPlayer player = (ServerPlayer) haystack.player().orElseThrow(() -> new IllegalArgumentException("forge networking is still retard"));
        return new Pair<>(CommonServerNetworkHandler.getHandler(player.connection), player);
    }
}
