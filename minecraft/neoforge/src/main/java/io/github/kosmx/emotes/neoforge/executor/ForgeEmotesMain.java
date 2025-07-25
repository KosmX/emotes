package io.github.kosmx.emotes.neoforge.executor;

import io.github.kosmx.emotes.server.services.InstanceService;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgeEmotesMain implements InstanceService {
    @Override
    public Path getGameDirectory() {
        return FMLLoader.getGamePath();
    }


    @Override
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
