package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.arch.executor.AbstractEmotesMain;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.forge.ForgeWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ForgeEmotesMain extends AbstractEmotesMain {
    final Side side;

    public ForgeEmotesMain(Side side){
        this.side = side;
    }

    @Override
    public Logger getLogger() {
        return ForgeWrapper.LOGGER::log;
    }

    @Override
    public IClientMethods getClientMethods() {
        return isClient() ? new ForgeClientMethods() : null;
    }

    @Override
    public boolean isClient() {
        return side.isClient();
    }

    @Override
    public Path getGameDirectory() {
        return Paths.get("");
    }

}
