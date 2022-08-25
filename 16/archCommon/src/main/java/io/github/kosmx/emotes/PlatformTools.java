package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class PlatformTools {
    @ExpectPlatform
    public static boolean isPlayerAnimLoaded() {
        throw new AssertionError();
    }
}
