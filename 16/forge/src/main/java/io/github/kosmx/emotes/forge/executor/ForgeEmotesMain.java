package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.arch.executor.AbstractEmotesMain;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.forge.ForgeWrapper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

import java.nio.file.Path;

public class ForgeEmotesMain extends AbstractEmotesMain {
    @Override
    public Logger getLogger() {
        return ForgeWrapper::log;
    }

    @Override
    public IClientMethods getClientMethods() {
        return isClient() ? new ForgeClientMethods() : null;
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
