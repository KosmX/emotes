package io.github.kosmx.emotes.server.services;

import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.impl.InstanceServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface InstanceService extends IEmotecraftService {
    InstanceService INSTANCE = ServiceLoaderUtil.loadService(InstanceService.class, InstanceServiceImpl::new);

    Path getGameDirectory();

    default Path getExternalEmoteDir() {
        return getGameDirectory().resolve(Serializer.getConfig().emotesDir.get());
    }

    default Path getConfigFolder() {
        String directoryName = "config";
        try {
            directoryName = System.getProperty("emotecraftConfigDir", "config");
            if (directoryName.equals("pluginDefault")) {
                directoryName = "plugins/emotecraft";
            }
        } catch(Throwable ignore) {
        }
        Path configDir = getGameDirectory().resolve(directoryName);
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch(IOException ignored) {
            }
        }
        return configDir;
    }

    default Path getConfigPath() {
        return getConfigFolder().resolve("emotecraft.json");
    }
}
