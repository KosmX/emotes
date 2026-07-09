package io.github.kosmx.emotes.server.services;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.impl.InstanceServiceImpl;
import org.redlance.common.services.AdvancedService;
import org.redlance.common.services.ServiceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface InstanceService extends AdvancedService {
    InstanceService INSTANCE = ServiceUtils.loadService(InstanceService.class, InstanceServiceImpl::new);

    Path getGameDirectory();

    default Path getCacheDirectory() {
        return getGameDirectory().resolve(CommonData.MOD_ID);
    }

    default Path getExternalEmoteDir() {
        return getGameDirectory().resolve(Serializer.getConfig().emotesDir.get());
    }

    default Path getConfigPath() {
        String directoryName = "config";

        try {
            directoryName = System.getProperty("emotecraftConfigDir", "config");
            if (directoryName.equals("pluginDefault")) {
                directoryName = "plugins/emotecraft";
            }
        } catch(Throwable ignore) {
        }

        if (!Files.exists(getGameDirectory().resolve(directoryName))) {
            try {
                Files.createDirectories(getGameDirectory().resolve(directoryName));
            } catch(IOException ignored) {
            }
        }
        return getGameDirectory().resolve(directoryName).resolve("emotecraft.json");
    }
}
