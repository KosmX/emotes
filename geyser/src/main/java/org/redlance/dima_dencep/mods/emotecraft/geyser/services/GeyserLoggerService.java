package org.redlance.dima_dencep.mods.emotecraft.geyser.services;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;

import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import java.util.logging.Level;

public class GeyserLoggerService implements LoggerService {
    @Override
    public void log(Level level, String msg, Throwable throwable) {
        EmotecraftExt.getInstance().logger().severe(msg, throwable);
    }

    @Override
    public void log(Level level, String msg) {
        EmotecraftExt.getInstance().logger().info(msg);
    }

    @Override
    public boolean isActive() {
        return EmotecraftExt.getInstance() != null;
    }

    @Override
    public int getPriority() {
        return ServiceLoaderUtil.DEFAULT_PRIORITY / 2;
    }
}
