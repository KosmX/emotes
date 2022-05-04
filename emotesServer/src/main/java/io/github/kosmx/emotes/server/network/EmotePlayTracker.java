package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;
import lombok.Getter;

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

    @Getter
    private EmoteData currentEmote = null;

    private Instant startTime = null;

    public void setPlayedEmote(@Nullable EmoteData data) {
        currentEmote = data;
        if (data == null) {
            startTime = null;
        }
        else {
            startTime = Instant.now();
        }
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
            return null;
        }
        return new Pair<>(currentEmote, tick);
    }

}
