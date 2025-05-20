package io.github.kosmx.emotes.api;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;

/**
 * @param startTime animation playback start time
 * @param offsetTime whether to apply {@code startTime} to the loop
 */
public record PlayingAnimationData(KeyframeAnimation currentEmote, int tick, Instant startTime, boolean offsetTime, boolean forced) {
    public PlayingAnimationData(NetData data) {
        this(data.emoteData, data.tick, data.startInstant(), data.offsetTime, data.isForced);
    }

    public PlayingAnimationData(KeyframeAnimation currentEmote, int tick, boolean offsetTime, boolean forced) {
        this(currentEmote, tick, Instant.now(), offsetTime, forced);
    }

    public PlayingAnimationData(KeyframeAnimation currentEmote, int tick, boolean forced) {
        this(currentEmote, tick, false, forced);
    }

    public PlayingAnimationData(KeyframeAnimation currentEmote, boolean forced) {
        this(currentEmote, 0, forced);
    }

    public PlayingAnimationData(KeyframeAnimation currentEmote) {
        this(currentEmote, false);
    }

    public EmotePacket.Builder preparePacket() {
        return new EmotePacket.Builder()
                .configureToStreamEmote(currentEmote())
                .setStartTime(startTime(), offsetTime())
                .configureEmoteTick(tick());
    }

    public int offsetTick(Temporal now) {
        KeyframeAnimation data = currentEmote();
        int t = calculatedTick(now);
        if (data.isInfinite() && t > data.returnToTick) {
            t = (t - data.returnToTick) % (data.endTick - data.returnToTick + 1) + data.returnToTick;
        }
        return t;
    }

    public int calculatedTick(Temporal now) {
        return PlayingAnimationData.calculateTick(startTime(), now) + this.tick;
    }

    public boolean isPlayingAt(Temporal time) {
        return currentEmote().isPlayingAt(calculatedTick(time));
    }

    public static int calculateTick(Temporal startTime, Temporal newStartTime) {
        return (int) (Duration.between(startTime, newStartTime).toMillis() / 50);
    }
}
