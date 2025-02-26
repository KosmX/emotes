package io.github.kosmx.emotes.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.main.MainClientInit;
import io.github.kosmx.emotes.main.MainLoader;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@EventBusSubscriber(Dist.CLIENT)
public class ClientInit {

    static KeyMapping openMenuKey;
    static KeyMapping stopEmote;
    static KeyMapping debugKey = null;
    static Consumer<Minecraft> keyBindingFunction;

    static void initClient(ModContainer container, IEventBus modEventBus) {
        initKeyBinding();
        //modEventBus.register(new ClientInit());
        modEventBus.addListener(ClientInit::keyBindingRegister);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (minecraft, screen) -> new EmoteMenu(screen)
        );
    }

    @SubscribeEvent
    public static void endClientTick(ClientTickEvent.Post event){
        MainLoader.tick();
    }

    @SubscribeEvent
    public static void keyListenerEvent(InputEvent.Key event){
        keyBindingFunction.accept(null);
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientNetwork.INSTANCE.disconnect();
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientNetwork.INSTANCE.configureOnPlay(event.getConnection()::send);
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
                if(PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || Minecraft.getInstance().player == Minecraft.getInstance().getCameraEntity()){
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
