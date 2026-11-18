package io.github.kosmx.emotes.arch.gui.animation;

import net.minecraft.util.EasingType;
import net.minecraft.util.Mth;

public record Tween(float delayTicks, float durationTicks, EasingType easing) {
    public Tween(float durationTicks, EasingType easing) {
        this(0.0F, durationTicks, easing);
    }

    public float at(float elapsedTicks) {
        return this.easing.apply(Mth.clamp((elapsedTicks - this.delayTicks) / this.durationTicks, 0.0F, 1.0F));
    }

    public boolean isOver(float elapsedTicks) {
        return elapsedTicks >= this.delayTicks + this.durationTicks;
    }
}
