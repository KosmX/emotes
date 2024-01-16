package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;

public final class PlatformTools {

    @ExpectPlatform
    public static boolean isPlayerAnimLoaded() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static INetworkInstance getClientNetworkController() {
        throw new AssertionError();
    }
}
