package io.github.kosmx.emotes.server.network;


import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Pair;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;

/**
 * Server side emote state tracking
 * It uses {@link Instant}
 * By using instant, tracking is mostly immune to server lags, tick drops
 * However susceptible to system clock changes.
 * And less demanding for a large server
 *
 */
public class EmotePlayTracker {

    private KeyframeAnimation currentEmote = null;

    private Instant startTime = null;

    private boolean isForced = false;

    /**
     * Set the currently played emote.
     * @param data Emote, null if stop playing
     */
    public void setPlayedEmote(@Nullable KeyframeAnimation data, boolean isForced) {
        currentEmote = data;
        if (data == null) {
            startTime = null;
            this.isForced = false;
        }
        else {
            startTime = Instant.now();
            this.isForced = isForced;
        }
    }

    /**
     * Is the currently played emote forced
     * Returns false if not playing emote
     * a.k.a. disallow the user play a different emote
     * @return true if forced, false if not playing any emote.
     */
    public boolean isForced() {
        if( getPlayedEmote() != null) {
            return isForced;
        }
        else return false;
    }

    /**
     * Get the currently played emote and the tick time
     * @return null if not playing emote
     */
    @Nullable
    public Pair<KeyframeAnimation, Integer> getPlayedEmote() {
        if (currentEmote == null) return null;
        int tick = (int)(Duration.between(startTime, Instant.now()).toMillis() / 50);
        if (!currentEmote.isInfinite() && currentEmote.getLength() <= tick) {
            currentEmote = null;
            startTime = null;
            isForced = false;
            return null;
        }
        return new Pair<>(currentEmote, tick);
    }

}
