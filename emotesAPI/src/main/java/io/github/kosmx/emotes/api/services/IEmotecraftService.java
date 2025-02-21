package io.github.kosmx.emotes.api.services;

import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;

public interface IEmotecraftService {
    boolean isActive();

    default int getPriority() {
        return ServiceLoaderUtil.DEFAULT_PRIORITY;
    }

    default String getName() {
        return getClass().getName();
    }
}
