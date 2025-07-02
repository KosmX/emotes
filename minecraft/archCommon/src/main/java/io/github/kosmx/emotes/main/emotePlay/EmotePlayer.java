package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Modified keyframe animation player to play songs with animations
 */
public class EmotePlayer extends PlayerAnimationController {
    @Nullable
    private MinecraftNbsPlayer song;

    public EmotePlayer(AbstractClientPlayer player) {
        super(player, (controller, state, animSetter) -> PlayState.STOP);
        /*if (emote.extraData.containsKey("song")) {
            this.song = new MinecraftNbsPlayer((Song) emote.extraData.get("song"), noteConsumer, 0);
        } else {
            this.song = null;
        }*/
    }

    /*@Override
    public void tick() {
        super.tick();
        if (this.song != null && isActive() && !this.song.isRunning()) {
            Component nowPlaying = this.song.getNowPlaying();
            if (nowPlaying != null) Minecraft.getInstance().gui.setNowPlaying(nowPlaying);
            this.song.start();
        }
    }*/

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void stop() {
        stopTriggeredAnimation();
        super.stop();
        this.animationQueue.clear();
        if (this.song != null) this.song.stop();
    }

    /**
     * Is emotePlayer running
     *
     * @param emote EmotePlayer, can be null
     * @return is running
     */
    public static boolean isRunningEmote(@Nullable EmotePlayer emote) {
        return emote != null && emote.isPlayingTriggeredAnimation();
    }

    @SuppressWarnings("UnstableApiUsage")
    public @Nullable Animation getData() {
        AnimationProcessor.QueuedAnimation animation = getCurrentAnimation();
        if (animation == null) return null;
        return animation.animation();
    }
}
