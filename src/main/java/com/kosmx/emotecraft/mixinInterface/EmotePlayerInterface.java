package com.kosmx.emotecraft.mixinInterface;

import com.kosmx.emotecraft.Emote;

import javax.annotation.Nullable;

public interface EmotePlayerInterface {

    void playEmote(Emote emote);

    @Nullable
    Emote getEmote();

    int getLastUpdated();

    void resetLastUpdated();
}
