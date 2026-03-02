package io.github.kosmx.emotes.neoforge.network;

import io.github.kosmx.emotes.arch.network.*;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = CommonData.MOD_ID)
public class ForgeNetwork {
    @SubscribeEvent
    public static void registerPlay(final RegisterPayloadHandlersEvent event) {
        event.registrar("emotecraft") // Play networking
                .optional()
                .playBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER,
                        (arg, playPayloadContext) -> CommonServerNetworkHandler.getInstance().receiveMessage(arg.packet(), playPayloadContext.player()),
                        (arg, playPayloadContext) -> ClientNetwork.INSTANCE.receiveMessage(arg.packet())
                )

                .optional()
                .playBidirectional(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER,
                        (arg, playPayloadContext) -> CommonServerNetworkHandler.getInstance().receiveStreamMessage(arg.packet(), playPayloadContext.player()),
                        (arg, playPayloadContext) -> {
                            try {
                                ClientNetwork.INSTANCE.receiveStreamMessage(arg.packet(), playPayloadContext.listener()::send);
                            } catch (IOException e) {
                                CommonData.LOGGER.error("", e);
                            }
                        }
                )

                .optional()
                .configurationBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER,
                        (arg, configurationPayloadContext) -> {
                            try {
                                var message = arg.packet().data;
                                if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");

                                ((EmotesMixinConnection) configurationPayloadContext.connection()).emotecraft$setVersions(message.versions);
                                UniversalEmoteSerializer.preparePackets(message.versions)
                                        .map(NetworkPlatformTools::playPacket)
                                        .forEach(configurationPayloadContext.connection()::send);

                                configurationPayloadContext.finishCurrentTask(ConfigTask.TYPE);
                            } catch (IOException e) {
                                CommonData.LOGGER.error("", e);
                                configurationPayloadContext.disconnect(Component.literal(CommonData.MOD_ID + ": " + e.getMessage()));
                            }
                        },
                        (arg, configurationPayloadContext) -> {
                            try {
                                ClientNetwork.INSTANCE.receiveConfigMessage(arg.packet(), configurationPayloadContext.listener()::send);
                            } catch (IOException e) {
                                CommonData.LOGGER.error("", e);
                            }
                        }
                )

                .optional()
                .configurationToClient(NetworkPlatformTools.STREAM_CHANNEL_ID, EmotePacketPayload.STREAM_CHANNEL_READER, (arg, configurationPayloadContext) -> {
                    try {
                        ClientNetwork.INSTANCE.receiveStreamMessage(arg.packet(), configurationPayloadContext.listener()::send);
                    } catch (IOException e) {
                        CommonData.LOGGER.error("", e);
                    }
                });
    }

    @SubscribeEvent
    public static void registerNetworkConfigTask(final RegisterConfigurationTasksEvent event) {
        if (event.getListener().hasChannel(NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
            event.register(new ConfigTask());
        } else {
            CommonData.LOGGER.debug("Client doesn't support emotes, ignoring");
        }
    }
}
