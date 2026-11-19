package io.github.kosmx.emotes.arch.gui.animation;

import net.minecraft.util.Mth;

public final class Animatable {
    private final Tween tween;
    private final Timeline timeline = new Timeline();
    private float from;
    private float to;

    public Animatable(float value, Tween tween) {
        this.tween = tween;
        this.from = value;
        this.to = value;
    }

    public void animateTo(float target) {
        if (target == this.to) return;
        this.from = get();
        this.to = target;
        this.timeline.restart();
    }

    public float get() {
        return Mth.lerp(this.tween.at(this.timeline.elapsedTicks()), this.from, this.to);
    }

    public boolean isRunning() {
        return this.from != this.to && !this.tween.isOver(this.timeline.elapsedTicks());
    }

    public boolean isAt(float value) {
        return this.to == value && !isRunning();
    }
}
