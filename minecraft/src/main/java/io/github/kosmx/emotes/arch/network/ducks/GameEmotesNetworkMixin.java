package io.github.kosmx.emotes.arch.network.ducks;

import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import org.jetbrains.annotations.NotNull;

public interface GameEmotesNetworkMixin {
    @NotNull
    default PlayerNetworkInstance emotecraft$getGameNetworkInstance() {
        throw new AssertionError();
    }
}
