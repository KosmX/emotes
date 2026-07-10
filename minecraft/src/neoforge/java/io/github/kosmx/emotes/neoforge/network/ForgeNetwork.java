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
                .playBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, null,
                        (arg, ctx) -> CommonServerNetworkHandler.getInstance().receiveMessage(arg.packet(), ctx.player()),
                        (arg, ctx) -> ClientNetwork.INSTANCE.receiveMessage(arg.packet(), null)
                )

                .optional()
                .playBidirectional(NetworkPlatformTools.STREAM_CHANNEL_ID, null,
                        (arg, ctx) -> CommonServerNetworkHandler.getInstance().receiveStreamMessage(arg.packet(), ctx.player()),
                        (arg, ctx) -> NetworkPlatformTools.tryReceive(
                                () -> ClientNetwork.INSTANCE.receiveStreamMessage(arg.packet(), ctx.listener()::send)
                        )
                )

                .optional()
                .configurationBidirectional(NetworkPlatformTools.EMOTE_CHANNEL_ID, null,
                        (arg, ctx) -> {
                            try {
                                var message = arg.packet().data;
                                if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");

                                ((EmotesMixinConnection) ctx.connection()).emotecraft$setVersions(message.versions);
                                UniversalEmoteSerializer.preparePackets(message.versions)
                                        .map(NetworkPlatformTools::playPacket)
                                        .forEach(ctx.connection()::send);

                                ctx.finishCurrentTask(ConfigTask.TYPE);
                            } catch (Exception e) {
                                CommonData.LOGGER.error("Invalid Emotecraft packet!", e);
                                ctx.disconnect(Component.literal(CommonData.MOD_ID + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())));
                            }
                        },
                        (arg, ctx) -> NetworkPlatformTools.tryReceive(
                                () -> ClientNetwork.INSTANCE.receiveConfigMessage(arg.packet(), ctx.listener()::send)
                        )
                )

                .optional()
                .configurationToClient(NetworkPlatformTools.STREAM_CHANNEL_ID, null,
                        (arg, ctx) -> NetworkPlatformTools.tryReceive(
                                () -> ClientNetwork.INSTANCE.receiveStreamMessage(arg.packet(), ctx.listener()::send)
                        )
                );
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
