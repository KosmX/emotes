package io.github.kosmx.emotes.services;

public interface IEmotecraftService {
    boolean isActive();

    default int getPriority() {
        return ServiceLoaderUtil.DEFAULT_PRIORITY;
    }

    default String getName() {
        return "Provider " + getClass().getName();
    }
}
