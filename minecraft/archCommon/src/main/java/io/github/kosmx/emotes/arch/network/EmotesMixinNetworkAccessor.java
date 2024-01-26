package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface EmotesMixinNetworkAccessor {
    @NotNull EmotePlayTracker emotecraft$getEmoteTracker();

    @NotNull HashMap<Byte, Byte> emotecraft$getRemoteVersions();

    void emotecraft$setVersions(@Nullable HashMap<Byte, Byte> map);
}
