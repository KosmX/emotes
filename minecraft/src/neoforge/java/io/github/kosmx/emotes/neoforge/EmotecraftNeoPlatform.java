package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.EmotecraftModPlatform;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

@SuppressWarnings("UnstableApiUsage")
public final class EmotecraftNeoPlatform implements EmotecraftModPlatform {
    @Override
    public String getModVersion(String modid) {
        ModFileInfo info = FMLLoader.getCurrent().getLoadingModList().getModFileById(modid);
        if (info == null) return modid.toUpperCase() + "-UNKNOWN-NEOFORGE";
        return info.versionString();
    }

    @Override
    public String getPlatformName() {
        return "neoforge";
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.neoforged.fml.loading.FMLLoader");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
