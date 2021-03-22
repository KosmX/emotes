package com.kosmx.emotes.fabric;

import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.fabric.executor.EmotesMain;
import com.kosmx.emotes.fabric.executor.FabricClientMethods;
import com.kosmx.emotes.fabric.gui.screen.ingame.FastChosseScreen;
import com.kosmx.emotes.fabric.network.ClientNetworkInstance;
import com.kosmx.emotes.fabric.network.ServerNetwork;
import com.kosmx.emotes.main.MainLoader;
import com.kosmx.emotes.main.network.ClientEmotePlay;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Initializer implements ModInitializer {

    public static final Logger logger = Logger.getLogger(CommonData.MOD_NAME);

    static KeyBinding openMenuKey;
    static KeyBinding stopEmote;
    static Consumer<MinecraftClient> keyBindingFunction;

    @Override
    public void onInitialize() {
        EmoteInstance.instance = new EmotesMain();
        MainLoader.main(null);
        setupFabric(); //Init keyBinding, networking etc...

    }

    private static void setupFabric(){
        initKeyBinding();

        ClientNetworkInstance.networkInstance.init(); //init network

        ServerNetwork.instance.init();

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

    public static void log(Level level, String msg){
        logger.log(level, msg);
    }
}
