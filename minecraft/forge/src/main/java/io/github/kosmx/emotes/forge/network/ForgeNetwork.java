package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

/**
 * Networking hell
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetwork {

    @SubscribeEvent
    public static void registerPlay(final RegisterPayloadHandlerEvent event) {
        var emotes = event.registrar("emotes");
        emotes.optional().play(NetworkPlatformTools.EMOTE_CHANNEL_ID, EmotePacketPayload.EMOTE_CHANNEL_READER, handler -> {
            handler.client((arg, playPayloadContext) -> ClientNetwork.INSTANCE.receiveMessage(arg.bytes(), null));
            handler.server((arg, playPayloadContext) -> )
        });
    }
}
