package com.kosmx.emotecraft.mixinInterface;

import com.kosmx.emotecraft.model.EmotePlayer;
import com.kosmx.emotecraftCommon.EmoteData;

import javax.annotation.Nullable;

public interface EmotePlayerInterface {

    void playEmote(EmoteData emote);

    @Nullable
    EmotePlayer getEmote();

    void resetLastUpdated();

    boolean isPlayingEmote();

    void stopEmote();

}
