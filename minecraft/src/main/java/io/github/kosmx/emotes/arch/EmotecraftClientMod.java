package io.github.kosmx.emotes.arch;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.BaseClientNetwork;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
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
    }

    protected void onClientTick(Minecraft minecraft) {
        if (tick++ % 21 == 20) BaseClientNetwork.checkQueue();

        if (OPEN_MENU_KEY.consumeClick()) {
            if(PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || minecraft.player == minecraft.getCameraEntity()) {
                minecraft.gui.setScreen(new FastMenuScreen(null));
            }
        }

        if (STOP_EMOTE_KEY.consumeClick()) {
            ClientEmotePlay.clientStopLocalEmote();
        }
    }

    public static CompletableFuture<Void> loadEmotes() {
        return CompletableFuture.supplyAsync(UniversalEmoteSerializer::loadEmotes, Util.ioPool())
                .thenAccept(_ -> {
                    EmoteHolder.clearEmotes();
                    EmoteHolder.addEmoteToList(UniversalEmoteSerializer.getLoadedEmotes(), null);
                })
                .thenRun(EmotecraftClientMod::migrateLegacyBinds)
                .exceptionally(th -> {
                    CommonData.LOGGER.error("Failed to reload emotes!", th);
                    return null;
                });
    }

    /** One-off: resolve legacy UUID key binds / wheel slots to holders once emotes have loaded, then persist the new format. */
    private static void migrateLegacyBinds() {
        ClientConfig config = PlatformTools.getConfig();
        boolean changed = false;

        if (config.legacyKeyBinds != null) {
            config.legacyKeyBinds.forEach((key, uuid) -> {
                EmoteHolder holder = EmoteHolder.list.get(uuid);
                if (holder != null) config.keyBinds.put(key, holder);
            });
            config.legacyKeyBinds = null;
            changed = true;
        }

        if (config.legacyFastMenu != null) {
            for (int j = 0; j < config.legacyFastMenu.length && j < config.fastMenuEmotes.length; j++) {
                for (int i = 0; i < config.legacyFastMenu[j].length; i++) {
                    UUID uuid = config.legacyFastMenu[j][i];
                    if (uuid == null) continue;
                    EmoteHolder holder = EmoteHolder.list.get(uuid);
                    if (holder != null) config.fastMenuEmotes[j][i] = holder;
                }
            }
            config.legacyFastMenu = null;
            changed = true;
        }

        if (changed) Serializer.INSTANCE.saveConfig();
    }

    public static int getTick() {
        return tick;
    }
}
