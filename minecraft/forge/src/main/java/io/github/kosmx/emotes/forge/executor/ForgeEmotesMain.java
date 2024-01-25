package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.forge.ForgeWrapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.nio.file.Path;

public class ForgeEmotesMain extends EmoteInstance {
    @Override
    public Logger getLogger() {
        return ForgeWrapper::log;
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public Path getGameDirectory() {
        return FMLLoader.getGamePath();
    }

}
