package com.kosmx.emotes.executor.emotePlayer;

import com.kosmx.emotes.common.emote.EmoteData;

import javax.annotation.Nullable;
import java.util.UUID;

//Every player will be IEmotePlayer
public interface IEmotePlayer<T> {

    void playEmote(EmoteData emote);

    @Nullable
    T getEmote();

    void resetLastUpdated();

    boolean isPlayingEmote();

    void stopEmote();

    UUID getUUID();

    boolean isMainPlayer();

}
