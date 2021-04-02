package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.fabric.Initializer;
import io.github.kosmx.emotes.fabric.executor.types.GettersImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class EmotesMain extends EmoteInstance {
    @Override
    public Logger getLogger() {
        return Initializer::log;
    }

    @Override
    public IDefaultTypes getDefaults() {
        return new Defaults();
    }

    @Override
    public IGetters getGetters() {
        return new GettersImpl();
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
    public File getExternalEmoteDir() {
        return getGameDirectory().resolve("emotes").toFile();
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("emotecraft.json");
    }
}
