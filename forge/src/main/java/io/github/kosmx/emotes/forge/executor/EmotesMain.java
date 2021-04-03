package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.forge.ForgeWrapper;
import io.github.kosmx.emotes.forge.executor.types.GettersImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.File;
import java.nio.file.Path;

public class EmotesMain extends EmoteInstance {
    @Override
    public Logger getLogger() {
        return ForgeWrapper.LOGGER::log;
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
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public Path getGameDirectory() {
        return FMLLoader.getGamePath();
    }

    @Override
    public File getExternalEmoteDir() {
        return getGameDirectory().resolve("emotes").toFile();
    }
}
