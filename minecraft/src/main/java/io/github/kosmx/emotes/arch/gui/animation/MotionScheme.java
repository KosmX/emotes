package io.github.kosmx.emotes.arch.gui.animation;

import net.minecraft.util.EasingType;

/** The library app's Material 3 Expressive motion scheme, in ticks. */
public final class MotionScheme {
    public static final Tween FAST_EFFECTS = new Tween(2, EasingType.LINEAR); // Its fast effects spring settles in about 100 ms
    public static final Tween FAST_SPATIAL = new Tween(5, EasingType.OUT_BACK); // Its expressive fast spatial one in about 250 ms, overshooting by 10% as OUT_BACK does

    private MotionScheme() {}
}
