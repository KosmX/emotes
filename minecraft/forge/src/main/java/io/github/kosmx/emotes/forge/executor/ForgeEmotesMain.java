package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.forge.ForgeWrapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.nio.file.Path;
import java.util.logging.Level;

public class ForgeEmotesMain extends EmoteInstance {

    private static final Logger logger = new Logger() {
        @Override
        public void writeLog(Level level, String msg, Throwable throwable) {
            ForgeWrapper.log(level, msg, throwable);
        }

        @Override
        public void writeLog(Level level, String msg) {
            ForgeWrapper.log(level, msg);
        }
    };

    @Override
    public Logger getLogger() {
        return logger;
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
