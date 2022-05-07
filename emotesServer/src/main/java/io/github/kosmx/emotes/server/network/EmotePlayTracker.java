package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;

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

    private EmoteData currentEmote = null;

    private Instant startTime = null;

    private boolean isForced = false;

    /**
     * Set the currently played emote.
     * @param data Emote, null if stop playing
     */
    public void setPlayedEmote(@Nullable EmoteData data, boolean isForced) {
        currentEmote = data;
        if (data == null) {
            startTime = null;
            isForced = false;
        }
        else {
            startTime = Instant.now();
            isForced = isForced;
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
    public Pair<EmoteData, Integer> getPlayedEmote() {
        if (currentEmote == null) return null;
        Instant now = Instant.now();
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
