package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.common.emote.EmoteData;
import lombok.Getter;

import javax.annotation.Nullable;

public class ServerPlayerTracker {

    @Getter
    EmoteData currentEmote = null;

    int emoteTick = 0;
    boolean isInfinite = false;

    public void setPlayedEmote(@Nullable EmoteData data) {
        currentEmote = data;
        if (data == null) {
            emoteTick = 0;
            isInfinite = false;
        }
        else {
            emoteTick = 0;
            this.isInfinite = data.isInfinite;
        }
    }

    public void tick() {
        if (currentEmote == null) return;
        emoteTick++;
        if (!isInfinite && emoteTick > currentEmote.getLength() ) {
            setPlayedEmote(null);
        }
    }

}
