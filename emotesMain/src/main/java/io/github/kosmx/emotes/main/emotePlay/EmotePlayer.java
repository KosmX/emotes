package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.opennbs.SoundPlayer;
import io.github.kosmx.emotes.common.opennbs.format.Layer;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayer;
import io.github.kosmx.playerAnim.layered.EmoteDataPlayer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

// abstract to extend it in every environments
public abstract class EmotePlayer<T> extends EmoteDataPlayer implements IEmotePlayer {
    @Nullable
    final SoundPlayer song;

    /**
     *
     * @param emote emote to play
     * @param noteConsumer {@link Layer.Note} consumer
     * @param t begin playing from tick
     */
    public EmotePlayer(EmoteData emote, Consumer<Layer.Note> noteConsumer, int t) {
        super(emote, t);
        if (emote.song != null) {
            this.song = new SoundPlayer(emote.song, noteConsumer, 0);
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
    /**
     * Is emotePlayer running
     *
     * @param emote EmotePlayer, can be null
     * @return is running
     */
    public static boolean isRunningEmote(@Nullable EmotePlayer emote) {
        return emote != null && emote.isActive();
    }

    @Override
    public boolean isRunning() {
        return isActive();
    }
}