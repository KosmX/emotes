package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.extension.GeyserExtensionContainer;
import org.geysermc.geyser.platform.standalone.GeyserStandaloneBootstrap;
import org.redlance.common.utils.ReflectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
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

    // Nominal descriptors of the classes referenced by the injected bytecode.
    // Referenced by name only (ClassDesc.of loads nothing), so GeyserExtensionLoader
    // stays unloaded until patchClass() redefines it.
    private static final ClassDesc CD_EXTENSION_LOADER = ClassDesc.of("org.geysermc.geyser.extension.GeyserExtensionLoader");
    private static final ClassDesc CD_EXTENSION_CONTAINER = ClassDesc.of("org.geysermc.geyser.extension.GeyserExtensionContainer");
    private static final ClassDesc CD_EXTENSION_DESCRIPTION = ClassDesc.of("org.geysermc.geyser.extension.GeyserExtensionDescription");
    private static final ClassDesc CD_GEYSER_EXT_EVENT_BUS = ClassDesc.of("org.geysermc.geyser.extension.event.GeyserExtensionEventBus");
    private static final ClassDesc CD_GEYSER_IMPL = ClassDesc.of("org.geysermc.geyser.GeyserImpl");
    private static final ClassDesc CD_GEYSER_EVENT_BUS = ClassDesc.of("org.geysermc.geyser.event.GeyserEventBus");
    private static final ClassDesc CD_EVENT_BUS = ClassDesc.of("org.geysermc.geyser.api.event.EventBus");
    private static final ClassDesc CD_EXTENSION_EVENT_BUS = ClassDesc.of("org.geysermc.geyser.api.event.ExtensionEventBus");
    private static final ClassDesc CD_EXTENSION = ClassDesc.of("org.geysermc.geyser.api.extension.Extension");
    private static final ClassDesc CD_EXTENSION_MANAGER = ClassDesc.of("org.geysermc.geyser.api.extension.ExtensionManager");
    private static final ClassDesc CD_EMOTECRAFT_EXT = ClassDesc.of("org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt");
    private static final ClassDesc CD_INPUT_STREAM_READER = ClassDesc.of("java.io.InputStreamReader");
    private static final ClassDesc CD_INPUT_STREAM = ClassDesc.of("java.io.InputStream");
    private static final ClassDesc CD_READER = ClassDesc.of("java.io.Reader");
    private static final ClassDesc CD_PATH = ClassDesc.of("java.nio.file.Path");
    private static final ClassDesc CD_MAP = ClassDesc.of("java.util.Map");
    private static final ClassDesc CD_RUNTIME_EXCEPTION = ClassDesc.of("java.lang.RuntimeException");
    private static final ClassDesc CD_THROWABLE = ClassDesc.of("java.lang.Throwable");

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

    /**
     * Injects the following into the top of {@code GeyserExtensionLoader#loadAllExtensions}, registering
     * Emotecraft as if it were a regular jar extension (the classes are already on the dev classpath):
     * <pre>{@code
     * EmotecraftExt extension = new EmotecraftExt();
     * try {
     *     InputStreamReader reader = new InputStreamReader(EmotecraftExt.class.getResourceAsStream("/extension.yml"));
     *     GeyserExtensionDescription description = GeyserExtensionDescription.fromYaml(reader);
     *     reader.close();
     *
     *     Path path = this.extensionsDirectory.resolve(description.id());
     *     GeyserExtensionEventBus extensionEventBus = new GeyserExtensionEventBus(GeyserImpl.getInstance().eventBus(), extension);
     *     GeyserExtensionContainer container = this.setup(extension, description, path, extensionEventBus);
     *
     *     this.extensionContainers.put(extension, container);
     *     this.register(extension, extensionManager);
     * } catch (Throwable t) {
     *     throw new RuntimeException(t);
     * }
     * }</pre>
     * The code runs inside {@code GeyserExtensionLoader} itself, so the private {@code setup},
     * {@code extensionsDirectory} and {@code extensionContainers} members are reachable directly.
     */
    private static byte[] patch(byte[] bytes) {
        ClassFile classFile = ClassFile.of();
        ClassModel classModel = classFile.parse(bytes);

        return classFile.transformClass(classModel, ClassTransform.transformingMethods(
                method -> method.methodName().equalsString("loadAllExtensions"),
                MethodTransform.transformingCode(new CodeTransform() {
                    @Override
                    public void accept(CodeBuilder builder, CodeElement element) {
                        builder.with(element); // Keep the original method body untouched
                    }

                    @Override
                    public void atStart(CodeBuilder code) {
                        // slot 0 = this (GeyserExtensionLoader), slot 1 = extensionManager
                        int extSlot = code.allocateLocal(TypeKind.REFERENCE);
                        int readerSlot = code.allocateLocal(TypeKind.REFERENCE);
                        int descSlot = code.allocateLocal(TypeKind.REFERENCE);
                        int pathSlot = code.allocateLocal(TypeKind.REFERENCE);
                        int containerSlot = code.allocateLocal(TypeKind.REFERENCE);
                        int throwableSlot = code.allocateLocal(TypeKind.REFERENCE);

                        // EmotecraftExt extension = new EmotecraftExt();
                        code.new_(CD_EMOTECRAFT_EXT)
                                .dup()
                                .invokespecial(CD_EMOTECRAFT_EXT, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void))
                                .astore(extSlot);

                        code.trying(tryBlock -> {
                            // InputStreamReader reader = new InputStreamReader(EmotecraftExt.class.getResourceAsStream("/extension.yml"));
                            tryBlock.new_(CD_INPUT_STREAM_READER)
                                    .dup()
                                    .ldc(CD_EMOTECRAFT_EXT)
                                    .ldc("/extension.yml")
                                    .invokevirtual(ConstantDescs.CD_Class, "getResourceAsStream", MethodTypeDesc.of(CD_INPUT_STREAM, ConstantDescs.CD_String))
                                    .invokespecial(CD_INPUT_STREAM_READER, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, CD_INPUT_STREAM))
                                    .astore(readerSlot);

                            // GeyserExtensionDescription description = GeyserExtensionDescription.fromYaml(reader);
                            tryBlock.aload(readerSlot)
                                    .invokestatic(CD_EXTENSION_DESCRIPTION, "fromYaml", MethodTypeDesc.of(CD_EXTENSION_DESCRIPTION, CD_READER))
                                    .astore(descSlot);

                            // reader.close();
                            tryBlock.aload(readerSlot)
                                    .invokevirtual(CD_INPUT_STREAM_READER, "close", MethodTypeDesc.of(ConstantDescs.CD_void));

                            // Path path = this.extensionsDirectory.resolve(description.id());
                            tryBlock.aload(0)
                                    .getfield(CD_EXTENSION_LOADER, "extensionsDirectory", CD_PATH)
                                    .aload(descSlot)
                                    .invokevirtual(CD_EXTENSION_DESCRIPTION, "id", MethodTypeDesc.of(ConstantDescs.CD_String))
                                    .invokeinterface(CD_PATH, "resolve", MethodTypeDesc.of(CD_PATH, ConstantDescs.CD_String))
                                    .astore(pathSlot);

                            // GeyserExtensionContainer container = this.setup(extension, description, path,
                            //         new GeyserExtensionEventBus(GeyserImpl.getInstance().eventBus(), extension));
                            tryBlock.aload(0)
                                    .aload(extSlot)
                                    .aload(descSlot)
                                    .aload(pathSlot)
                                    .new_(CD_GEYSER_EXT_EVENT_BUS)
                                    .dup()
                                    .invokestatic(CD_GEYSER_IMPL, "getInstance", MethodTypeDesc.of(CD_GEYSER_IMPL))
                                    .invokevirtual(CD_GEYSER_IMPL, "eventBus", MethodTypeDesc.of(CD_GEYSER_EVENT_BUS))
                                    .aload(extSlot)
                                    .invokespecial(CD_GEYSER_EXT_EVENT_BUS, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, CD_EVENT_BUS, CD_EXTENSION))
                                    .invokevirtual(CD_EXTENSION_LOADER, "setup", MethodTypeDesc.of(CD_EXTENSION_CONTAINER, CD_EXTENSION, CD_EXTENSION_DESCRIPTION, CD_PATH, CD_EXTENSION_EVENT_BUS))
                                    .astore(containerSlot);

                            // this.extensionContainers.put(extension, container);
                            tryBlock.aload(0)
                                    .getfield(CD_EXTENSION_LOADER, "extensionContainers", CD_MAP)
                                    .aload(extSlot)
                                    .aload(containerSlot)
                                    .invokeinterface(CD_MAP, "put", MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object, ConstantDescs.CD_Object))
                                    .pop();

                            // this.register(extension, extensionManager);
                            tryBlock.aload(0)
                                    .aload(extSlot)
                                    .aload(1)
                                    .invokevirtual(CD_EXTENSION_LOADER, "register", MethodTypeDesc.of(ConstantDescs.CD_void, CD_EXTENSION, CD_EXTENSION_MANAGER));
                        }, catchBuilder -> catchBuilder.catchingAll(catchBlock ->
                                // throw new RuntimeException(t);
                                catchBlock.astore(throwableSlot)
                                        .new_(CD_RUNTIME_EXCEPTION)
                                        .dup()
                                        .aload(throwableSlot)
                                        .invokespecial(CD_RUNTIME_EXCEPTION, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, CD_THROWABLE))
                                        .athrow()
                        ));
                    }
                })
        ));
    }

    public static void patchClass(Class<?> nearClass, String name, UnaryOperator<byte[]> patcher) throws ReflectiveOperationException, IOException {
        try (InputStream is = Objects.requireNonNull(nearClass.getClassLoader().getResourceAsStream(name))) {
            byte[] bytecode = patcher.apply(is.readAllBytes());
            MethodHandles.privateLookupIn(nearClass, MethodHandles.lookup()).defineClass(bytecode);
        }
    }
}
