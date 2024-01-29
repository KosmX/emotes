package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import org.jetbrains.annotations.NotNull;

public interface EmotesMixinNetwork {
    @NotNull EmotePlayTracker emotecraft$getEmoteTracker();

    @NotNull EmotesMixinConnection emotecraft$getConnection();
}
