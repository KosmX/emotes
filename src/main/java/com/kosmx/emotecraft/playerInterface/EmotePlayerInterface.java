package com.kosmx.emotecraft.playerInterface;

import com.kosmx.emotecraft.Emote;

import javax.annotation.Nullable;

public interface EmotePlayerInterface {

    void playEmote(Emote emote);

    @Nullable
    Emote getEmote();
}
