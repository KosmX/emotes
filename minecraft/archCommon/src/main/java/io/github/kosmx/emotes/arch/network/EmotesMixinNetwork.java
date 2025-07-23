package io.github.kosmx.emotes.arch.network;

import org.jetbrains.annotations.NotNull;

public interface EmotesMixinNetwork {
    @NotNull
    ModdedServerPlayNetwork emotecraft$getServerNetworkInstance();
}
