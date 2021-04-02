package io.github.kosmx.emotes.executor.emotePlayer;

import io.github.kosmx.emotes.common.emote.EmoteData;

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
