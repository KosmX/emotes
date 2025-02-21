package io.github.kosmx.emotes.mc.services.impl;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.CommonData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Logger4jService implements LoggerService {
    public static final Logger LOGGER = LogManager.getLogger(CommonData.MOD_NAME);

    private static final ConcurrentMap<java.util.logging.Level, Level> LEVELS = new ConcurrentHashMap<>(9);
    static {
        LEVELS.put(java.util.logging.Level.ALL, Level.ALL);
        LEVELS.put(java.util.logging.Level.FINEST, Level.TRACE);
        LEVELS.put(java.util.logging.Level.FINER, Level.TRACE);
        LEVELS.put(java.util.logging.Level.FINE, Level.DEBUG);
        LEVELS.put(java.util.logging.Level.CONFIG, Level.DEBUG);
        LEVELS.put(java.util.logging.Level.INFO, Level.INFO);
        LEVELS.put(java.util.logging.Level.WARNING, Level.WARN);
        LEVELS.put(java.util.logging.Level.SEVERE, Level.ERROR);
        LEVELS.put(java.util.logging.Level.OFF, Level.OFF);
    }

    @Override
    public void log(java.util.logging.Level level, String msg, Throwable throwable) {
        Logger4jService.LOGGER.log(LEVELS.getOrDefault(level, Level.WARN), msg, throwable);
    }

    @Override
    public void log(java.util.logging.Level level, String msg) {
        Logger4jService.LOGGER.log(LEVELS.getOrDefault(level, Level.WARN), msg);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
