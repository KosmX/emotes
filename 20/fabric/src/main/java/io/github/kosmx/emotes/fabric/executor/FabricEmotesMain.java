package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.arch.executor.AbstractEmotesMain;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.fabric.FabricWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricEmotesMain extends AbstractEmotesMain {
    @Override
    public Logger getLogger() {
        return FabricWrapper::log;
    }

    @Override
    public IClientMethods getClientMethods() {
        return isClient() ? new FabricClientMethods() : null;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigPath() {
        if (System.getProperty("emotecraftConfigDir") != null) {
            return super.getConfigPath();
        }

        return FabricLoader.getInstance().getConfigDir().resolve("emotecraft.json");
    }
}
