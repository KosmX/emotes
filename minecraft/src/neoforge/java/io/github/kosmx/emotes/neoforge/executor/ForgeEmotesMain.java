package io.github.kosmx.emotes.neoforge.executor;

import io.github.kosmx.emotes.server.services.InstanceService;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgeEmotesMain implements InstanceService {
    @Override
    public Path getGameDirectory() {
        return FMLLoader.getCurrent().getGameDir();
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("emotecraft.json");
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
