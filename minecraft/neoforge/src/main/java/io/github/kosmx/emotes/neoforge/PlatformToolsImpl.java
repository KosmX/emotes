package io.github.kosmx.emotes.neoforge;

import net.neoforged.fml.loading.FMLLoader;

public class PlatformToolsImpl {
    public static boolean hasSearchables() {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById("searchables") != null;
    }
}
