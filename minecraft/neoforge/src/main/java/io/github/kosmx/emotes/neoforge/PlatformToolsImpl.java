package io.github.kosmx.emotes.neoforge;

import net.neoforged.fml.loading.LoadingModList;

public class PlatformToolsImpl {
    public static boolean hasSearchables() {
        return LoadingModList.get().getModFileById("searchables") != null;
    }
}
