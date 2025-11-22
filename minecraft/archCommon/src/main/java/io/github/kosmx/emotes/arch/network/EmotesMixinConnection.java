package io.github.kosmx.emotes.arch.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface EmotesMixinConnection {
    @NotNull
    Map<Byte, Byte> emotecraft$getRemoteVersions();

    void emotecraft$setVersions(@Nullable Map<Byte, Byte> map);
}
