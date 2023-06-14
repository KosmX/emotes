package io.github.kosmx.emotes.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class PlatformToolsImpl {
    public static boolean isPlayerAnimLoaded() {
        return FabricLoader.getInstance().isModLoaded("player-animator");
    }
}
