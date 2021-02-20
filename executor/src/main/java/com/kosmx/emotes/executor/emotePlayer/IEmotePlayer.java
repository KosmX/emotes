package com.kosmx.emotes.executor.emotePlayer;

public interface IEmotePlayer {
    boolean isRunning();

    static boolean isRunningEmote(IEmotePlayer emotePlayer){
        return emotePlayer != null && emotePlayer.isRunning();
    }

    void tick();

    boolean isLoopStarted();
}
