package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.relocation;

import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.event.ExtensionEventBus;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionManager;
import org.geysermc.geyser.api.extension.exception.InvalidExtensionException;
import org.geysermc.geyser.extension.GeyserExtensionClassLoader;
import org.geysermc.geyser.extension.GeyserExtensionContainer;
import org.geysermc.geyser.extension.GeyserExtensionDescription;
import org.geysermc.geyser.extension.GeyserExtensionLoader;
import org.geysermc.geyser.extension.event.GeyserExtensionEventBus;
import org.redlance.common.utils.ReflectUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class RemappingExt implements Extension {
    private static final VarHandle CLASSLOADERS = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVarHandle(
            GeyserExtensionLoader.class, "classLoaders", Map.class
    ));
    private static final VarHandle DESCRIPTION = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVarHandle(
            GeyserExtensionClassLoader.class, "description", GeyserExtensionDescription.class
    ));

    private static final VarHandle EXTENSION_CONTAINERS = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVarHandle(
            GeyserExtensionLoader.class, "extensionContainers", Map.class
    ));
    private static final MethodHandle REGISTER = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVirtual(
            GeyserExtensionLoader.class, "register", MethodType.methodType(void.class, Extension.class, ExtensionManager.class)
    ));
    private static final MethodHandle SETUP = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVirtual(
            GeyserExtensionLoader.class, "setup", MethodType.methodType(GeyserExtensionContainer.class, Extension.class, GeyserExtensionDescription.class, Path.class, ExtensionEventBus.class)
    ));

    private final Map<String, GeyserExtensionClassLoader> classLoaders = new HashMap<>();

    public RemappingExt() throws Throwable {
        try (GeyserExtensionClassLoader classLoader = getClassLoaders().remove(CommonData.MOD_ID)) {
            GeyserExtensionDescription description = getDescriptionFromClassLoader(classLoader);
            ReflectUtils.setRecordField(description, "main", "org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt");

            registerExtension(loadExtension(Path.of(classLoader.getURLs()[0].toURI()), description));
            CommonData.LOGGER.error("Please ignore the error below, emotecraft is loading as it should!");
        } catch (Throwable th) {
            th.printStackTrace();
            CommonData.LOGGER.warn("Failed to boostrap emotecraft!", th);
        }
        throw new UnsupportedOperationException("Bootstrap complete"); // prevert future loading
    }

    @SuppressWarnings("unchecked")
    public Map<String, GeyserExtensionClassLoader> getClassLoaders() {
        return (Map<String, GeyserExtensionClassLoader>) CLASSLOADERS.get(extensionLoader());
    }

    public GeyserExtensionDescription getDescriptionFromClassLoader(GeyserExtensionClassLoader classLoader) {
        return (GeyserExtensionDescription) DESCRIPTION.get(classLoader);
    }

    @SuppressWarnings("unchecked")
    public void registerExtension(GeyserExtensionContainer container) {
        ((Map<Extension, GeyserExtensionContainer>) EXTENSION_CONTAINERS.get(extensionLoader())).put(container.extension(), container);
        try {
            REGISTER.invoke(extensionLoader(), container.extension(), extensionManager());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public GeyserExtensionContainer loadExtension(Path path, GeyserExtensionDescription description) throws Throwable {
        if (path == null) {
            throw new InvalidExtensionException("Path is null");
        }

        if (Files.notExists(path)) {
            throw new InvalidExtensionException(new NoSuchFileException(path.toString()) + " does not exist");
        }

        Path parentFile = path.getParent();

        // Extension folders used to be created by name; this changes them to the ID
        Path oldDataFolder = parentFile.resolve(description.name());
        Path dataFolder = parentFile.resolve(description.id());

        if (Files.exists(oldDataFolder) && Files.isDirectory(oldDataFolder) && !oldDataFolder.equals(dataFolder)) {
            try {
                Files.move(oldDataFolder, dataFolder, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new InvalidExtensionException("Failed to move data folder for extension " + description.name(), e);
            }
        }

        if (Files.exists(dataFolder) && !Files.isDirectory(dataFolder)) {
            throw new InvalidExtensionException("The folder " + dataFolder + " is not a directory and is the data folder for the extension " + description.name() + "!");
        }

        final GeyserExtensionClassLoader loader;
        try {
            loader = new GeyserExtensionClassLoader((GeyserExtensionLoader) extensionLoader(), new RemappingClassLoader(extensionLoader().getClass().getClassLoader(), path, extensionLoader().getClass()), Path.of("~/notexist"), description);
        } catch (Throwable e) {
            throw new InvalidExtensionException(e);
        }

        this.classLoaders.put(description.id(), loader);

        try {
            final Extension extension = loader.load();
            return (GeyserExtensionContainer) SETUP.invoke(extensionLoader(), extension, description, dataFolder, new GeyserExtensionEventBus(GeyserImpl.getInstance().eventBus(), extension));
        } catch (Throwable e) {
            // if the extension failed to load, remove its classloader and close it.
            this.classLoaders.remove(description.id()).close();
            throw e;
        }
    }
}
