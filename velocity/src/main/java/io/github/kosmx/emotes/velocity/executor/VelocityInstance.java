package io.github.kosmx.emotes.velocity.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;

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
    public boolean isClient() {
        return false;
    }

    @Override
    public Path getGameDirectory() {
        return Paths.get("");
    }
}
