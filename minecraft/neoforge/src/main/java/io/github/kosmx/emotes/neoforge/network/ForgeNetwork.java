package io.github.kosmx.emotes.neoforge.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;

import java.io.IOException;
import java.util.logging.Level;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ForgeNetwork {
    @SubscribeEvent
    public static void registerPlay(final RegisterPayloadHandlersEvent event) {
        event.registrar("emotecraft") // Play networking
                .optional()
                .playBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER, new DirectionalPayloadHandler<>(
                        (arg, playPayloadContext) -> ClientNetwork.INSTANCE.receiveMessage(arg.unwrapBytes()),
                        (arg, playPayloadContext) -> CommonServerNetworkHandler.getInstance().receiveMessage(arg.unwrapBytes(), playPayloadContext.player())
                ))

                .optional()
                .playBidirectional(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER, new DirectionalPayloadHandler<>(
                        (arg, playPayloadContext) -> {
                            try {
                                ClientNetwork.INSTANCE.receiveStreamMessage(arg.bytes(), null);
                            } catch (IOException e) {
                                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                            }
                        },
                        (arg, playPayloadContext) -> CommonServerNetworkHandler.getInstance().receiveStreamMessage(arg.unwrapBytes(), playPayloadContext.player())
                ))

                .optional()
                .configurationBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER, new DirectionalPayloadHandler<>(
                        (arg, configurationPayloadContext) -> {
                            try {
                                ClientNetwork.INSTANCE.receiveConfigMessage(arg.bytes(), p -> configurationPayloadContext.listener().send(p));
                            } catch (IOException e) {
                                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                            }
                        },
                        (arg, configurationPayloadContext) -> {
                            try {
                                var message = new EmotePacket.Builder().build().read(arg.bytes());
                                if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");

                                ((EmotesMixinConnection) configurationPayloadContext.connection()).emotecraft$setVersions(message.versions);
                                UniversalEmoteSerializer.preparePackets(message.versions).forEach(buffer ->
                                        configurationPayloadContext.connection().send(NetworkPlatformTools.playPacket(buffer))
                                );
                                configurationPayloadContext.finishCurrentTask(ConfigTask.TYPE);
                            } catch (IOException e) {
                                LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                                configurationPayloadContext.disconnect(Component.literal(CommonData.MOD_ID + ": " + e.getMessage()));
                            }
                        }
                ))

                .optional()
                .configurationToClient(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER, (arg, configurationPayloadContext) -> {
                    try {
                        ClientNetwork.INSTANCE.receiveStreamMessage(arg.bytes(), p -> configurationPayloadContext.listener().send(p));
                    } catch (IOException e) {
                        LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                    }
                });
    }

    @SubscribeEvent
    public static void registerNetworkConfigTask(final RegisterConfigurationTasksEvent event) {
        if (event.getListener().hasChannel(NetworkPlatformTools.EMOTE_CHANNEL_ID) ||
                event.getListener().hasChannel(NetworkPlatformTools.STREAM_CHANNEL_ID)) {

            event.register(new ConfigTask());
        } else {
            LoggerService.INSTANCE.log(Level.FINE, "Client doesn't support emotes, ignoring");
        }
    }
}
