package io.github.kosmx.playerAnim.layered;

import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;

/**
 * An entry in {@link AnimationStack}, used to get the animated parts current transform
 */
public interface IAnimation {

    /**
     * Animation tick, on lag free client 20 [tick/sec]
     * You can get the animations time from other places, but it will be invoked when the animation is ACTIVE
     */
    default void tick(){}

    /**
     * Is the animation currently active.
     * Tick will only be invoked when ACTIVE
     * @return active
     */
    boolean isActive();

    /**
     * Get the transformed value to a model part, transform type.
     * @param modelName The questionable model part
     * @param type      Transform type
     * @param tickDelta Time since the last tick. 0-1
     * @param value0    The value before the transform. For identity transform return with it.
     * @return The new transform value
     */
    Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0);

    /**
     * Called before rendering a character
     * @param tickDelta Time since the last tick. 0-1
     */
    void setupAnim(float tickDelta);

}
