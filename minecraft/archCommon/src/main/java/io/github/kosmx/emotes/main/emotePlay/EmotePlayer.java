package io.github.kosmx.emotes.main.emotePlay;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;

import io.github.kosmx.emotes.api.PlayingAnimationData;
import io.github.kosmx.emotes.arch.mixin.KeyframeAnimationPlayerAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.function.Consumer;

// modified keyframe animation player to play songs with animations
public class EmotePlayer extends KeyframeAnimationPlayer {
    public final PlayingAnimationData data;

    @Nullable
    public final MinecraftNbsPlayer song;

    protected boolean timeSynced;

    /**
     *
     * @param data emote to play
     * @param noteConsumer {@link Note} consumer
     */
    public EmotePlayer(PlayingAnimationData data, Consumer<Note> noteConsumer) {
        super(data.currentEmote(), data.tick());
        this.data = data;

        if (data.currentEmote().extraData.get("song") instanceof Song song0) {
            this.song = new MinecraftNbsPlayer(song0, noteConsumer, 0);
        } else {
            this.song = null;
        }
    }

    @Override
    public void tick() {
        if (this.data.offsetTime() && isLoopStarted() && !this.timeSynced) {
            int offsetTick = this.data.offsetTick(Instant.now());
            if (getData().returnToTick > offsetTick) { // Debug
                System.out.println("Invalid tick " + offsetTick);
            }
            ((KeyframeAnimationPlayerAccessor) this).setCurrentTick(offsetTick);
            this.timeSynced = true;
        } else super.tick();

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