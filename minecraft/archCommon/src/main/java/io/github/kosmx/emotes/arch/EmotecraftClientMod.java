package io.github.kosmx.emotes.arch;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

public class EmotecraftClientMod {
    protected static final KeyMapping.Category KEYBIND_CATEGORY = KeyMapping.Category.register(McUtils.newIdentifier("keybinding")); // key.category.emotecraft.keybinding

    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.emotecraft.fastchoose", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEYBIND_CATEGORY
    );
    public static final KeyMapping STOP_EMOTE_KEY = new KeyMapping(
            "key.emotecraft.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEYBIND_CATEGORY
    );

    private static int tick = 0;

    protected void onInitializeClient() {
        EmotecraftClientMod.loadEmotes();
        ClientPacketManager.init(); // initialize proxy service
    }

    protected void onClientTick(Minecraft minecraft) {
        if (tick++ % 21 == 20) ClientEmotePlay.checkQueue();

        if (OPEN_MENU_KEY.consumeClick()) {
            if(PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || minecraft.player == minecraft.getCameraEntity()) {
                minecraft.setScreen(new FastMenuScreen(null));
            }
        }

        if (STOP_EMOTE_KEY.consumeClick()) {
            ClientEmotePlay.clientStopLocalEmote();
        }
    }

    public static CompletableFuture<Void> loadEmotes() {
        return CompletableFuture.supplyAsync(UniversalEmoteSerializer::loadEmotes, Util.ioPool())
                .thenAccept(emotes -> {
                    EmoteHolder.clearEmotes();
                    EmoteHolder.addEmoteToList(UniversalEmoteSerializer.getLoadedEmotes(), null);
                })
                .exceptionally(th -> {
                    CommonData.LOGGER.error("Failed to reload emotes!", th);
                    return null;
                });
    }

    public static int getTick() {
        return tick;
    }
}
