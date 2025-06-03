package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.PlayingAnimationData;
import org.jetbrains.annotations.Nullable;
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
    protected PlayingAnimationData currentEmote = null;

    /**
     * Set the currently played emote.
     * @param data Emote, null if stop playing
     */
    public void setPlayedEmote(@Nullable PlayingAnimationData data) {
        this.currentEmote = data;
    }

    /**
     * Is the currently played emote forced
     * Returns false if not playing emote
     * a.k.a. disallow the user play a different emote
     * @return true if forced, false if not playing any emote.
     */
    public boolean isForced() {
        PlayingAnimationData data = getPlayedEmote();
        if (data == null) return false;
        return this.currentEmote.forced();
    }

    /**
     * Get the currently played emote and the tick time
     * @return null if not playing emote
     */
    @Nullable
    public PlayingAnimationData getPlayedEmote() {
        if (currentEmote == null) return null;
        if (!currentEmote.isPlayingAt(Instant.now())) {
            currentEmote = null;
            return null;
        }
        return this.currentEmote;
    }
}
