package com.kosmx.emotes.executor.emotePlayer;

import com.kosmx.emotes.common.emote.EmoteData;

public interface IEmotePlayer {
    boolean isRunning();

    static boolean isRunningEmote(IEmotePlayer emotePlayer){
        return emotePlayer != null && emotePlayer.isRunning();
    }

    void tick();

    boolean isLoopStarted();

    EmoteData getData();

    int getTick();
}
