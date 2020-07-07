package com.kosmx.emotecraft.playerInterface;

import com.kosmx.emotecraft.Emote;

import javax.annotation.Nullable;

public interface ClientPlayerEmotes {

    boolean isPlayingEmote();

    void playEmote(Emote emote);

    Emote getEmote();
}
