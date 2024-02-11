package io.github.kosmx.emotes.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.arch.executor.ClientMethods;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.MainClientInit;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientInit {

    static KeyMapping openMenuKey;
    static KeyMapping stopEmote;
    static KeyMapping debugKey = null;
    static Consumer<Minecraft> keyBindingFunction;

    static void initClient(IEventBus modEventBus) {
        initKeyBinding();
        //modEventBus.register(new ClientInit());
        modEventBus.addListener(ClientInit::keyBindingRegister);
    }

    static void setupClient() {

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new EmoteMenu(screen)));
    }

    @SubscribeEvent
    public static void endClientTick(TickEvent.ClientTickEvent event){
        ClientMethods.tick++;
    }

    @SubscribeEvent
    public static void keyListenerEvent(InputEvent.Key event){
        keyBindingFunction.accept(null);
    }

    public static void keyBindingRegister(RegisterKeyMappingsEvent event) {
        event.register(openMenuKey);
        event.register(stopEmote);
        if (debugKey != null) event.register(debugKey);
    }

    private static void initKeyBinding(){
        openMenuKey = new KeyMapping("key.emotecraft.fastchoose", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.emotecraft.keybinding");
        //KeyBindingRegistryImpl.registerKeyBinding(openMenuKey);
        //ClientRegistry.registerKeyBinding(openMenuKey);


        stopEmote = new KeyMapping("key.emotecraft.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding");
        //ClientRegistry.registerKeyBinding(stopEmote);

        if(FMLLoader.getGamePath().resolve("emote.json").toFile().isFile()){ //Secret feature//
            debugKey = new KeyMapping("key.emotecraft.debug", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O,       //I don't know why... just
                    "category.emotecraft.keybinding");
            //ClientRegistry.registerKeyBinding(debugKey);
        }

        keyBindingFunction = client -> {

            if(openMenuKey.consumeClick()){
                if(((ClientConfig) EmoteInstance.config).alwaysOpenEmoteScreen.get() || Minecraft.getInstance().player == Minecraft.getInstance().getCameraEntity()){
                    Minecraft.getInstance().setScreen(new FastMenuScreen(null));
                }
            }
            if(stopEmote.consumeClick()){
                ClientEmotePlay.clientStopLocalEmote();
            }
            if(debugKey != null && debugKey.consumeClick()){
                MainClientInit.playDebugEmote();
            }
        };
    }
}
