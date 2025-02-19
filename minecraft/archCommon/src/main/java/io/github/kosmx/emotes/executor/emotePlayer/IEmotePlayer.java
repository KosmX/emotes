package io.github.kosmx.emotes.executor.emotePlayer;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

public interface IEmotePlayer {
    boolean isRunning();

    static boolean isRunningEmote(IEmotePlayer emotePlayer){
        return emotePlayer != null && emotePlayer.isRunning();
    }

    boolean isLoopStarted();

    void tick();

    KeyframeAnimation getData();

    int getTick();
}
