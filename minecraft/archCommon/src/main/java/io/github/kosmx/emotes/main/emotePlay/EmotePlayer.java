package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.State;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import org.jetbrains.annotations.Nullable;

/**
 * Modified keyframe animation player to play songs with animations
 */
public class EmotePlayer extends PlayerAnimationController {
    @Nullable
    private MinecraftNbsPlayer song;

    public boolean perspective = false;
    public boolean muteNbs = false;

    public EmotePlayer(Avatar avatar) {
        super(avatar, (controller, state, animSetter) -> PlayState.STOP);
    }

    @Override
    protected void setAnimation(RawAnimation rawAnimation, float startAnimFrom) {
        State state = getAnimationState();
        super.setAnimation(rawAnimation, startAnimFrom);
        this.animationState = state;
    }

    @Override
    protected void setupNewAnimation() {
        super.setupNewAnimation();

        Animation emote = getCurrentAnimationInstance();

        if (this.song != null) this.song.stop();
        if (emote != null && emote.data().has("song")) {
            this.song = new MinecraftNbsPlayer(this, emote.data().<NbsSong>get("song").orElseThrow());
        } else {
            this.song = null;
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void stop() {
        super.stop();
        stopTriggeredAnimation();
        this.animationQueue.clear();
        internalStop();
    }

    @Override
    public void process(AnimationData state) {
        super.process(state);
        if (!this.animationState.isActive()) internalStop();
    }

    private void internalStop() {
        if (this.perspective && PlatformTools.getPerspective() == PlatformTools.getConfig().getCameraType()) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            this.perspective = false;
        }
        if (this.song != null) this.song.stop();
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
    protected <T extends KeyFrameData> void handleCustomKeyframe(T[] keyframes, CustomKeyFrameEvents.@Nullable CustomKeyFrameHandler<T> main, CustomKeyFrameEvents.CustomKeyFrameHandler<T> event, float animationTick, AnimationData animationData) {
        if (this.avatar instanceof UnsafeMannequin) return;
        super.handleCustomKeyframe(keyframes, main, event, animationTick, animationData);
    }

    @Override
    protected void applyCustomPivotPoints() {
        if (this.song != null && !this.song.isFirstSongPlayed() && isActive() && !this.song.isRunning() && !this.muteNbs) {
            Component nowPlaying = this.song.getNowPlaying();
            if (nowPlaying != null) Minecraft.getInstance().gui.setNowPlaying(nowPlaying);
            this.song.setPaused(getAnimationState() == State.PAUSED);
            this.song.start();
        }
        super.applyCustomPivotPoints();
    }

    @Override
    public void pause() {
        super.pause();
        if (this.song != null) this.song.setPaused(true);
    }

    @Override
    public void unpause() {
        super.unpause();
        if (this.song != null) this.song.setPaused(false);
    }
}
