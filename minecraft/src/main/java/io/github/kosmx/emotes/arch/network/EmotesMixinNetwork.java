package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import org.jetbrains.annotations.NotNull;

public interface EmotesMixinNetwork {
    @NotNull
    default PlayerNetworkInstance emotecraft$getServerNetworkInstance() {
        throw new AssertionError();
    }
}
