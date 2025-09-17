package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;

/**
 * Makes a RawAnimation using resource locations instead of full animations
 */
public class PlayerRawAnimationBuilder {
    private final RawAnimation rawAnimation = RawAnimation.begin();

    public static PlayerRawAnimationBuilder begin() {
        return new PlayerRawAnimationBuilder();
    }

    /**
     * Append an animation to the animation chain, playing the named animation and stopping
     * or progressing to the next chained animation depending on the loop type set in the animation json
     *
     * @param animation The id of the animation to play once
     */
    public PlayerRawAnimationBuilder thenPlay(String animation) {
        return then(animation, Animation.LoopType.DEFAULT);
    }

    /**
     * Append an animation to the animation chain, playing the named animation and repeating it continuously until the animation is stopped by external sources
     *
     * @param animation The id of the animation to play on a loop
     */
    public PlayerRawAnimationBuilder thenLoop(String animation) {
        return then(animation, Animation.LoopType.LOOP);
    }

    /**
     * Appends a 'wait' animation to the animation chain
     * <p>
     * This causes the animatable to do nothing for a set period of time before performing the next animation
     *
     * @param ticks The number of ticks to 'wait' for
     */
    public PlayerRawAnimationBuilder thenWait(int ticks) {
        rawAnimation.thenWait(ticks);

        return this;
    }

    /**
     * Appends an animation to the animation chain, then has the animatable hold the pose at the end of the
     * animation until it is stopped by external sources
     *
     * @param animation The id of the animation to play and hold
     */
    public PlayerRawAnimationBuilder thenPlayAndHold(String animation) {
        return then(animation, Animation.LoopType.HOLD_ON_LAST_FRAME);
    }

    /**
     * Append an animation to the animation chain, playing the named animation <code>playCount</code> times,
     * then stopping or progressing to the next chained animation depending on the loop type set in the animation json
     *
     * @param animation The id of the animation to play X times
     * @param playCount The number of times to repeat the animation before proceeding
     */
    public PlayerRawAnimationBuilder thenPlayXTimes(String animation, int playCount) {
        for (int i = 0; i < playCount; i++) {
            then(animation, i == playCount - 1 ? Animation.LoopType.DEFAULT : Animation.LoopType.PLAY_ONCE);
        }

        return this;
    }

    /**
     * Append an animation to the animation chain, playing the named animation and proceeding based on the <code>loopType</code> parameter provided
     *
     * @param animation The id of the animation to play. <u>MUST</u> match the name of the animation in the <code>.animation.json</code> file.
     * @param loopType The loop type handler for the animation, overriding the default value set in the animation json
     */
    public PlayerRawAnimationBuilder then(String animation, Animation.LoopType loopType) {
        rawAnimation.then(PlayerAnimResources.getAnimation(animation), loopType);

        return this;
    }

    public RawAnimation build() {
        return rawAnimation;
    }
}
