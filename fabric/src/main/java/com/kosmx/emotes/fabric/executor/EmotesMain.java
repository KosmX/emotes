package com.kosmx.emotes.fabric.executor;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.Logger;
import com.kosmx.emotes.executor.dataTypes.IClientMethods;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import com.kosmx.emotes.executor.dataTypes.IGetters;
import com.kosmx.emotes.fabric.Initializer;
import com.kosmx.emotes.fabric.executor.types.GettersImpl;
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
