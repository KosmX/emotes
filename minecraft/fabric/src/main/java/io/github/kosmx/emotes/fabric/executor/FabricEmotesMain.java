package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.fabric.FabricWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.logging.Level;

public class FabricEmotesMain extends EmoteInstance {

    private static final Logger logger = new Logger() {
        @Override
        public void writeLog(Level level, String msg, Throwable throwable) {
            FabricWrapper.log(level, msg, throwable);
        }

        @Override
        public void writeLog(Level level, String msg) {
            FabricWrapper.log(level, msg);
        }
    };

    @Override
    public Logger getLogger() {
        return logger;
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
