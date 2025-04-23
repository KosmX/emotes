package io.github.kosmx.emotes.main.emotePlay;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

// modified keyframe animation player to play songs with animations
public class EmotePlayer extends KeyframeAnimationPlayer {
    @Nullable
    final MinecraftNbsPlayer song;

    /**
     *
     * @param emote emote to play
     * @param noteConsumer {@link Note} consumer
     * @param t begin playing from tick
     */
    public EmotePlayer(KeyframeAnimation emote, Consumer<Note> noteConsumer, int t) {
        super(emote, t);
        if (emote.extraData.containsKey("song")) {
            this.song = new MinecraftNbsPlayer((Song) emote.extraData.get("song"), noteConsumer, 0);
        } else {
            this.song = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.song != null && isActive() && !this.song.isRunning()) {
            Component nowPlaying = this.song.getNowPlaying();
            if (nowPlaying != null) Minecraft.getInstance().gui.setNowPlaying(nowPlaying);
            this.song.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (this.song != null) this.song.stop();
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