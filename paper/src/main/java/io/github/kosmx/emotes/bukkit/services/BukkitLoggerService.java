package io.github.kosmx.emotes.bukkit.services;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;

import java.util.logging.Level;

public class BukkitLoggerService implements LoggerService {
    private static final BukkitWrapper BUKKIT_WRAPPER = BukkitWrapper.getPlugin(BukkitWrapper.class);

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        BUKKIT_WRAPPER.getLogger().log(level, msg, throwable);
    }

    @Override
    public void log(Level level, String msg) {
        BUKKIT_WRAPPER.getLogger().log(level, msg);
    }

    @Override
    public boolean isActive() {
        return BUKKIT_WRAPPER != null;
    }

    @Override
    public int getPriority() {
        return ServiceLoaderUtil.DEFAULT_PRIORITY + 1;
    }
}
