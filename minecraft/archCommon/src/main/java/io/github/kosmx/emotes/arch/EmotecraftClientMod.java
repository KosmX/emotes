package io.github.kosmx.emotes.arch;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.PlayingAnimationData;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreen;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.MainLoader;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class EmotecraftClientMod {
    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.emotecraft.fastchoose", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.emotecraft.keybinding"
    );
    public static final KeyMapping STOP_EMOTE_KEY = new KeyMapping(
            "key.emotecraft.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding"
    );
    public static final KeyMapping PLAY_SAME_ANIM_KEY = new KeyMapping(
            "key.emotecraft.playsameanim", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding"
    );

    protected void onClientTick(Minecraft minecraft) {
        MainLoader.tick();

        if (!PLAY_SAME_ANIM_KEY.isUnbound() && minecraft.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof RemotePlayer player) {
            handlePlaySameAnimation(minecraft, player);
        }

        if (OPEN_MENU_KEY.consumeClick()) {
            if(PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || minecraft.player == minecraft.getCameraEntity()) {
                minecraft.setScreen(new FastMenuScreen(null));
            }
        }

        if (STOP_EMOTE_KEY.consumeClick()) {
            ClientEmotePlay.clientStopLocalEmote();
        }
    }

    private void handlePlaySameAnimation(Minecraft minecraft, RemotePlayer player) {
        if (minecraft.player != null && player.isPlayingEmote()) {
            PlayingAnimationData emotePlayer = player.emotecraft$getPlayingData();
            assert emotePlayer != null; // verified in isPlayingEmote()

            EmoteHolder sameHolder = EmoteHolder.getEmoteFromAnimation(emotePlayer.currentEmote());
            if (sameHolder == null) return;

            if (PLAY_SAME_ANIM_KEY.consumeClick()) {
                sameHolder.playEmote(minecraft.player, Objects.requireNonNull(player.emotecraft$getEmote()).getTick(), true);
            } else if (!minecraft.player.isPlayingEmote()) {
                minecraft.gui.setOverlayMessage(Component.translatable("key.emotecraft.playsameanim.subtitle",
                        PLAY_SAME_ANIM_KEY.getTranslatedKeyMessage()
                ), false);
            }
        }
    }
}
