package io.github.kosmx.emotes.arch.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface EmotesMixinConnection {
    @NotNull HashMap<Byte, Byte> emotecraft$getRemoteVersions();

    void emotecraft$setVersions(@Nullable HashMap<Byte, Byte> map);
}
