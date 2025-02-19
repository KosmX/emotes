package io.github.kosmx.emotes.api.services;

import io.github.kosmx.emotes.api.services.impl.SystemLoggerService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;

import java.util.logging.Level;

public interface LoggerService extends IEmotecraftService {
    LoggerService LOADED_SERVICE = ServiceLoaderUtil.loadService(LoggerService.class, SystemLoggerService::new);

    void log(Level level, String msg, Throwable throwable);
    void log(Level level, String msg);
}
