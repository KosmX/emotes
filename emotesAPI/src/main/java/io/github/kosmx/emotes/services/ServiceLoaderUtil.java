package io.github.kosmx.emotes.services;

import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ServiceLoaderUtil {
    public static final int DEFAULT_PRIORITY = 0;
    public static final int HIGHEST_SYSTEM_PRIORITY = 1000;
    public static final int LOWEST_SYSTEM_PRIORITY = -1000;

    public static <T extends IEmotecraftService> T loadService(Class<T> serviceClass, Supplier<? extends T> defaultService) {
        ModuleLayer layer = ServiceLoaderUtil.class.getModule().getLayer(); // NeoForge compat?
        ServiceLoader<T> loader = layer == null ? ServiceLoader.load(serviceClass) : ServiceLoader.load(layer, serviceClass);

        return loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(IEmotecraftService::isActive)
                .max(Comparator.comparingInt(IEmotecraftService::getPriority))
                .orElseGet(defaultService);
    }
}
