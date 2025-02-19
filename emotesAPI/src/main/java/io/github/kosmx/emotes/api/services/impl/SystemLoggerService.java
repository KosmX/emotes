package io.github.kosmx.emotes.api.services.impl;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;

import java.util.logging.Level;

public class SystemLoggerService implements LoggerService {
    @Override
    public void log(Level level, String msg, Throwable throwable) {
        System.out.println(msg);
        throwable.printStackTrace();
    }

    @Override
    public void log(Level level, String msg) {
        System.out.println(msg);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public int getPriority() {
        return ServiceLoaderUtil.LOWEST_SYSTEM_PRIORITY;
    }
}
