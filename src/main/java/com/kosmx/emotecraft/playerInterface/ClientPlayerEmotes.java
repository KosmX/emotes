package com.kosmx.emotecraft.playerInterface;

import com.kosmx.emotecraft.Emote;

import javax.annotation.Nullable;

public interface ClientPlayerEmotes {

    void playEmote(Emote emote);

    @Nullable
    Emote getEmote();
}
