package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.server.services.InstanceService;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricEmotesMain implements InstanceService {
    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("emotecraft.json");
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
