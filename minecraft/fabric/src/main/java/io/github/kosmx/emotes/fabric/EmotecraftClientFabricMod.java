package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.arch.ClientCommands;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class EmotecraftClientFabricMod extends EmotecraftClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_MENU_KEY);
        KeyBindingHelper.registerKeyBinding(STOP_EMOTE_KEY);

        super.onInitializeClient();
        ClientNetworkInstance.init(); //init network

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommands::register);
    }
}
