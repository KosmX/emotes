package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.common.CommonData;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.geysermc.geyser.extension.GeyserExtensionContainer;
import org.geysermc.geyser.platform.standalone.GeyserStandaloneBootstrap;
import org.redlance.common.utils.ReflectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Used to run Emotecraft in a dev environment.
 */
public class GeyserBootstrap {
    private static final Class<?> LEVEL_CLASS = ReflectUtils.uncheck(() -> Class.forName("org.apache.logging.log4j.Level"));
    private static final MethodHandle SET_LEVEL = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findStatic(
            Class.forName("org.apache.logging.log4j.core.config.Configurator"),
            "setLevel", MethodType.methodType(void.class, String.class, LEVEL_CLASS)
    ));
    private static final MethodHandle DEBUG_LEVEL = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findStaticGetter(
            LEVEL_CLASS, "DEBUG", LEVEL_CLASS
    ));

    static {
        System.setProperty("java.awt.headless", "true");
        try {
            SET_LEVEL.invoke(CommonData.LOGGER.getName(), DEBUG_LEVEL.invoke());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        GeyserBootstrap.patchClass(GeyserExtensionContainer.class, "org/geysermc/geyser/extension/GeyserExtensionLoader.class", GeyserBootstrap::patch);
        GeyserStandaloneBootstrap.main(args);
    }

    private static byte[] patch(byte[] bytes) {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ByteArrayClassPath("org.geysermc.geyser.extension.GeyserExtensionLoader", bytes));

        try {
            CtClass cc = pool.get("org.geysermc.geyser.extension.GeyserExtensionLoader");
            CtMethod method = cc.getDeclaredMethod("loadAllExtensions");
            String src = """
                    org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt extension = new org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt();
                    try {
                        java.io.InputStreamReader reader = new java.io.InputStreamReader(org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt.class.getResourceAsStream("/extension.yml"));
                        org.geysermc.geyser.extension.GeyserExtensionDescription description = org.geysermc.geyser.extension.GeyserExtensionDescription.fromYaml(reader);
                        reader.close();

                        java.nio.file.Path path = this.extensionsDirectory.resolve(description.id());
                        org.geysermc.geyser.api.event.EventBus eventBus = org.geysermc.geyser.GeyserImpl.getInstance().eventBus();
                        org.geysermc.geyser.extension.event.GeyserExtensionEventBus extensionEventBus = new org.geysermc.geyser.extension.event.GeyserExtensionEventBus(eventBus, extension);
                        org.geysermc.geyser.extension.GeyserExtensionContainer container = this.setup(extension, description, path, extensionEventBus);

                        this.extensionContainers.put(extension, container);
                        this.register(extension, $1);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }""";
            method.insertAfter(src, false);
            return cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void patchClass(Class<?> nearClass, String name, UnaryOperator<byte[]> patcher) throws ReflectiveOperationException, IOException {
        try (InputStream is = Objects.requireNonNull(nearClass.getClassLoader().getResourceAsStream(name))) {
            byte[] bytecode = patcher.apply(is.readAllBytes());
            MethodHandles.privateLookupIn(nearClass, MethodHandles.lookup()).defineClass(bytecode);
        }
    }
}
