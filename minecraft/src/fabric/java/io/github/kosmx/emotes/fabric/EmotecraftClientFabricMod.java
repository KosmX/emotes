package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.arch.ClientCommands;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import io.github.kosmx.emotes.arch.online.OnlineNetworkInstance;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class EmotecraftClientFabricMod extends EmotecraftClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(OPEN_MENU_KEY);
        KeyMappingHelper.registerKeyMapping(STOP_EMOTE_KEY);

        super.onInitializeClient();
        ClientNetworkInstance.init(); //init network

        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommands::register);

        // Online Emotes
        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> OnlineEmotes.connect());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> OnlineNetworkInstance.INSTANCE.disconnect());
        ClientConfigurationConnectionEvents.DISCONNECT.register((_, _) -> OnlineNetworkInstance.INSTANCE.disconnect()); // left while switching servers
        ClientEntityEvents.ENTITY_LOAD.register((entity, _) -> OnlineNetworkInstance.INSTANCE.onTrackingStart(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, _) -> OnlineNetworkInstance.INSTANCE.onTrackingEnd(entity));
    }
}
