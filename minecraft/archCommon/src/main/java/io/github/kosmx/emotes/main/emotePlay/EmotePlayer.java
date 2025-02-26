package io.github.kosmx.emotes.main.emotePlay;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.NBS;
import dev.kosmx.playerAnim.core.data.opennbs.SoundPlayer;
import dev.kosmx.playerAnim.core.data.opennbs.format.Layer;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

// modified keyframe animation player to play songs with animations
public class EmotePlayer extends KeyframeAnimationPlayer {
    @Nullable
    final SoundPlayer song;

    /**
     *
     * @param emote emote to play
     * @param noteConsumer {@link Layer.Note} consumer
     * @param t begin playing from tick
     */
    public EmotePlayer(KeyframeAnimation emote, Consumer<Layer.Note> noteConsumer, int t) {
        super(emote, t);
        if (emote.extraData.containsKey("song")) {
            this.song = new SoundPlayer((NBS) emote.extraData.get("song"), noteConsumer, 0);
        }
        else {
            this.song = null;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isActive()) {
            if (SoundPlayer.isPlayingSong(this.song)) song.tick();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if(this.perspective == 1){
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    /**
     * Is emotePlayer running
     *
     * @param emote EmotePlayer, can be null
     * @return is running
     */
    public static boolean isRunningEmote(@Nullable EmotePlayer emote) {
        return emote != null && emote.isActive();
    }
}