package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.arch.ClientCommands;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class EmotecraftClientFabricMod extends EmotecraftClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(OPEN_MENU_KEY);
        KeyMappingHelper.registerKeyMapping(STOP_EMOTE_KEY);

        super.onInitializeClient();
        ClientNetworkInstance.init(); //init network

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommands::register);
    }
}
