package io.github.kosmx.emotes.common.tools;

import io.github.kosmx.emotes.api.services.IEmotecraftService;

import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ServiceLoaderUtil {
    public static final int DEFAULT_PRIORITY = 0;
    public static final int HIGHEST_SYSTEM_PRIORITY = 1000;
    public static final int LOWEST_SYSTEM_PRIORITY = -1000;

    public static <T extends IEmotecraftService> Stream<T> loadServices(Class<T> serviceClass) {
        ModuleLayer layer = ServiceLoaderUtil.class.getModule().getLayer(); // NeoForge compat?

        ServiceLoader<T> loader = layer == null ? ServiceLoader.load(serviceClass,
                ServiceLoaderUtil.class.getClassLoader()
        ) : ServiceLoader.load(layer, serviceClass);

        return loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(IEmotecraftService::isActive)
                .sorted(Comparator.comparingInt(IEmotecraftService::getPriority));
    }

    public static <T extends IEmotecraftService> T loadService(Class<T> serviceClass, Supplier<? extends T> defaultService) {
        return ServiceLoaderUtil.loadServices(serviceClass).findFirst().orElseGet(defaultService);
    }
}
