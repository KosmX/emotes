package io.github.kosmx.playerAnim.impl;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;
import io.github.kosmx.playerAnim.layered.IAnimation;

/**
 * Tool to easily play animation to the player.
 */
public class AnimationPlayer {
    private final IAnimation animation;
    private float tickDelta = 0f;

    public AnimationPlayer(IAnimation animation) {
        this.animation = animation;
    }

    public void tick() {
        animation.tick();
    }

    public boolean isActive() {
        return animation.isActive();
    }

    public Vec3f get3DTransform(String modelName, TransformType type, Vec3f value0) {
        return animation.get3DTransform(modelName, type, this.tickDelta, value0);
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
        this.animation.setupAnim(tickDelta);
    }

    public Pair<Float, Float> getBend(String modelName) {
        Vec3f bendVec = this.get3DTransform(modelName, TransformType.BEND, Vec3f.ZERO);
        return new Pair<>(bendVec.getX(), bendVec.getY());
    }

}
