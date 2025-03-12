package io.github.kosmx.emotes.arch;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.main.MainLoader;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class EmotecraftClientMod {
    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.emotecraft.fastchoose", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.emotecraft.keybinding"
    );
    public static final KeyMapping STOP_EMOTE_KEY = new KeyMapping(
            "key.emotecraft.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding"
    );

    protected void onClientTick(Minecraft minecraft) {
        MainLoader.tick();

        if (OPEN_MENU_KEY.consumeClick()) {
            if(PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || minecraft.player == minecraft.getCameraEntity()) {
                minecraft.setScreen(new FastMenuScreen(null));
            }
        }

        if (STOP_EMOTE_KEY.consumeClick()) {
            ClientEmotePlay.clientStopLocalEmote();
        }
    }
}
