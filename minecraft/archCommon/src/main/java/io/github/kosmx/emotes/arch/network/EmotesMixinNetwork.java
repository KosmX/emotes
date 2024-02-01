package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import org.jetbrains.annotations.NotNull;

public interface EmotesMixinNetwork {

    @NotNull
    IServerNetworkInstance emotecraft$getServerNetworkInstance();
}
