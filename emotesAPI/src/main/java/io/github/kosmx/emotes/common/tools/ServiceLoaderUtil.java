package io.github.kosmx.emotes.common.tools;

import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.CommonData;

import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ServiceLoaderUtil {
    private static final Comparator<IEmotecraftService> COMPARATOR = Comparator.comparingInt(IEmotecraftService::getPriority);

    public static final int DEFAULT_PRIORITY = 0;
    public static final int HIGHEST_PRIORITY = 1000;
    public static final int LOWEST_PRIORITY = -1000;

    public static <T extends IEmotecraftService> Stream<T> loadServices(Class<T> serviceClass) {
        ModuleLayer layer = ServiceLoaderUtil.class.getModule().getLayer(); // NeoForge compat?

        ServiceLoader<T> loader = layer == null ? ServiceLoader.load(serviceClass,
                ServiceLoaderUtil.class.getClassLoader()
        ) : ServiceLoader.load(layer, serviceClass);

        return loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(IEmotecraftService::isActive);
    }

    public static <T extends IEmotecraftService> Stream<T> loadServicesSorted(Class<T> serviceClass) {
        return ServiceLoaderUtil.loadServices(serviceClass).sorted(COMPARATOR.reversed());
    }

    public static <T extends IEmotecraftService> T loadService(Class<T> serviceClass, Supplier<? extends T> defaultService) {
        return ServiceLoaderUtil.loadOptionalService(serviceClass).orElseGet(defaultService);
    }

    public static <T extends IEmotecraftService> T loadService(Class<T> serviceClass) {
        return ServiceLoaderUtil.loadOptionalService(serviceClass).orElseThrow();
    }

    public static <T extends IEmotecraftService> Optional<T> loadOptionalService(Class<T> serviceClass) {
        Optional<T> optional = ServiceLoaderUtil.loadServices(serviceClass).max(COMPARATOR);
        optional.ifPresent(service -> CommonData.LOGGER.debug("Selected service {} for {}", toString(service), serviceClass.getName()));
        return optional;
    }

    private static String toString(IEmotecraftService service) {
        return service.getName() + " (priority " + service.getPriority() + ")";
    }
}
