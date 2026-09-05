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
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.common.opus.OpusPackets;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

/**
 * Modified keyframe animation player to play songs with animations
 */
public class EmotePlayer extends PlayerAnimationController {
    private static final long RETRY_DELAY = 500L; // animation time runs backwards on a loop, wall time does not

    @Nullable
    private EmoteSoundInstance song;
    private long attempted;

    public boolean perspective = false;
    public boolean muteNbs = false;

    public EmotePlayer(Avatar avatar) {
        super(avatar, (controller, state, animSetter) -> PlayState.STOP);
    }

    @Override
    protected void setAnimation(RawAnimation rawAnimation, float startAnimFrom) {
        State state = getAnimationState();
        super.setAnimation(rawAnimation, startAnimFrom);
        // Only restore the previous state (e.g. PAUSED) while the controller is still
        // actually playing. If super.setAnimation stopped it (currentAnimation == null),
        // keep STOPPED instead of resurrecting RUNNING with no animation, which would
        // crash processCurrentAnimation with a NullPointerException.
        if (getCurrentAnimationInstance() != null) this.animationState = state;
    }

    @Override
    protected void setupNewAnimation() {
        super.setupNewAnimation();
        stopSound();
    }

    @Override
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
        CameraType changeTo = PlatformTools.getConfig().cameraType.get();
        if (this.perspective && !changeTo.isFirstPerson() && PlatformTools.getCameraType() == changeTo) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            this.perspective = false;
        }
        stopSound();
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
    public void pause() {
        super.pause();
        // No way to pause one instance; startSound picks the track back up at the right offset on unpause
        stopSound();
    }

    @Override
    protected void applyCustomPivotPoints() {
        startSound();
        super.applyCustomPivotPoints();
    }

    private void startSound() {
        if (this.muteNbs || !isActive() || !EmoteSoundInstance.audible(this.avatar)) return;

        SoundManager manager = Minecraft.getInstance().getSoundManager();
        if (this.song != null) {
            if (!this.song.isStopped() && (this.song.started() || manager.isActive(this.song))) return;
            this.song = null; // it stopped itself, or the engine turned it down; a fresh one starts in time
        }

        Animation emote = getCurrentAnimationInstance();
        if (emote == null || !(emote.data().getRaw(SongPacket.OPUS_KEY) instanceof OpusSound sound)) return;

        // Handing the engine an unfinished decode would strand a channel if the emote ends first
        OpusSound.DecodedSound decoded = sound.decoded();
        if (decoded == null) return;

        // Play can be refused for a whole emote, and asking again every frame notifies subtitles every frame
        long now = Util.getMillis();
        if (this.attempted != 0 && now - this.attempted < RETRY_DELAY) return;
        this.attempted = now;

        // Join wherever the animation already is, whether it started late or mid-emote
        int offset = (int) (getAnimationTime() * OpusPackets.SAMPLE_RATE);
        this.song = new EmoteSoundInstance(this.avatar, decoded, offset, sound.loopStart());
        manager.play(this.song);
    }

    private void stopSound() {
        this.attempted = 0; // the next emote starts its clock over
        if (this.song == null) return;

        this.song.stop();
        Minecraft.getInstance().getSoundManager().stop(this.song);
        this.song = null;
    }
}
