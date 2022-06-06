package io.github.kosmx.playerAnim.layered;

import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;

/**
 * A container to make swapping animation object easier
 * It will clone the behaviour of the held animation
 *
 * you can put endless AnimationContainer into each other
 * @param <T>
 */
public class AnimationContainer<T extends IAnimation> implements IAnimation {
    protected T anim;

    public AnimationContainer(T anim) {
        this.anim = anim;
    }


    public void setAnim(T newAnim) {
        this.anim = newAnim;
    }

    public T getAnim() {
        return this.anim;
    }

    @Override
    public boolean isActive() {
        return anim != null && anim.isActive();
    }

    @Override
    public void tick() {
        if (anim != null) anim.tick();
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        return anim == null ? value0 : anim.get3DTransform(modelName, type, tickDelta, value0);
    }

    @Override
    public void setupAnim(float tickDelta) {
        if (this.anim != null) this.anim.setupAnim(tickDelta);
    }
}
