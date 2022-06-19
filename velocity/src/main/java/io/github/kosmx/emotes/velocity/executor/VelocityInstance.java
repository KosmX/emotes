package io.github.kosmx.emotes.velocity.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VelocityInstance extends EmoteInstance {

    private final Logger logger;

    public VelocityInstance(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public IDefaultTypes getDefaults() {
        return null;
    }

    @Override
    public IGetters getGetters() {
        return null;
    }

    @Override
    public IClientMethods getClientMethods() {
        return null;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public Path getGameDirectory() {
        return Paths.get("");
    }
}
