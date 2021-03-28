package com.kosmx.emotes.fabric;

import com.kosmx.emotes.fabric.executor.FabricClientMethods;
import com.kosmx.emotes.fabric.gui.screen.ingame.FastChosseScreen;
import com.kosmx.emotes.fabric.network.ClientNetworkInstance;
import com.kosmx.emotes.main.network.ClientEmotePlay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientInit {

    static KeyBinding openMenuKey;
    static KeyBinding stopEmote;
    static Consumer<MinecraftClient> keyBindingFunction;

    static void initClient(){

        initKeyBinding();

        ClientNetworkInstance.networkInstance.init(); //init network

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FabricClientMethods.tick++;

            keyBindingFunction.accept(client);
        });
    }

    private static void initKeyBinding(){
        openMenuKey = new KeyBinding("key.emotecraft.fastchoose", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.emotecraft.keybinding");
        KeyBindingRegistryImpl.registerKeyBinding(openMenuKey);

        stopEmote = new KeyBinding("key.emotecraft.stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding");
        KeyBindingRegistryImpl.registerKeyBinding(stopEmote);

        keyBindingFunction = client -> {

            if(openMenuKey.wasPressed()){
                if(MinecraftClient.getInstance().player == MinecraftClient.getInstance().getCameraEntity()){
                    MinecraftClient.getInstance().openScreen(new FastChosseScreen(null));
                }
            }
            if(stopEmote.wasPressed()){
                ClientEmotePlay.clientStopLocalEmote();
            }
        };
    }
}
