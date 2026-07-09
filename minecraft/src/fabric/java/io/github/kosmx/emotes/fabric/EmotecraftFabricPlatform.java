package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.EmotecraftModPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class EmotecraftFabricPlatform implements EmotecraftModPlatform {
    @Override
    public String getModVersion(String modid) {
        return FabricLoader.getInstance().getModContainer(modid)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse(modid.toUpperCase() + "-UNKNOWN-FABRIC");
    }

    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
