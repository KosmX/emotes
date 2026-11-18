package io.github.kosmx.emotes.arch.gui.animation;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

/** Real time, as vanilla's own screen animations keep: a pause screen stops the game's ticks, not this. */
public final class Timeline {
    private long start = Util.getMillis();

    public void restart() {
        this.start = Util.getMillis();
    }

    public float elapsedTicks() {
        return (float) (Util.getMillis() - this.start) / SharedConstants.MILLIS_PER_TICK;
    }
}
