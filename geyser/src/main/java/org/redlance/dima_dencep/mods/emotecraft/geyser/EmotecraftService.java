package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.server.services.InstanceService;

import java.nio.file.Path;

public class EmotecraftService implements InstanceService {
    @Override
    public Path getGameDirectory() {
        return EmotecraftExt.getInstance().dataFolder();
    }

    @Override
    public Path getConfigPath() {
        return getGameDirectory().resolve("emotecraft.json");
    }

    @Override
    public boolean isServiceActive() {
        return EmotecraftExt.getInstance() != null && EmotecraftExt.getInstance().isEnabled();
    }

    @Override
    public int getPriority() {
        return InstanceService.super.getPriority() - 1;
    }
}
