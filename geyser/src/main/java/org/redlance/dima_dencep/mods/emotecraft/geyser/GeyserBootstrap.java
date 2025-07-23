package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.common.CommonData;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class GeyserBootstrap extends ClassLoader {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public GeyserBootstrap(ClassLoader parent) {
        super(parent);
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        GeyserBootstrap loader = new GeyserBootstrap(GeyserBootstrap.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(loader);

        Class<?> bootstrapClass = loader.loadClass("org.geysermc.geyser.platform.standalone.GeyserStandaloneBootstrap");
        Method main = bootstrapClass.getMethod("main", String[].class);
        main.invoke(null, (Object) args);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("jdk.") || name.startsWith("org.apache.logging.") || name.startsWith("com.sun.") || name.startsWith("org.slf4j.")) {
            return super.loadClass(name, resolve);
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) return c;

            try {
                byte[] classBytes = loadClassBytes(name);

                if (name.equals("org.geysermc.geyser.extension.GeyserExtensionLoader")) {
                    CommonData.LOGGER.info("Transforming {}", name);
                    ClassReader reader = new ClassReader(classBytes);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    ClassVisitor transformer = new GeyserExtensionLoaderClassVisitor(writer);
                    reader.accept(transformer, ClassReader.EXPAND_FRAMES);
                    classBytes = writer.toByteArray();
                }

                c = defineClass(name, classBytes, 0, classBytes.length);

                if (resolve) resolveClass(c);
                return c;
            } catch (ClassNotFoundException | IOException e) {
                return super.loadClass(name, resolve);
            } catch (Exception e) {
                throw new ClassNotFoundException("Failed to load or transform " + name, e);
            }
        }
    }

    private byte[] loadClassBytes(String name) throws IOException, ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream is = getParent().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new ClassNotFoundException("Resource not found by bootstrap loader: " + resourceName);
            }
            return is.readAllBytes();
        }
    }

    private static class GeyserExtensionLoaderClassVisitor extends ClassVisitor {
        public GeyserExtensionLoaderClassVisitor(ClassVisitor classVisitor) {
            super(ASM9, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (mv != null && "loadAllExtensions".equals(name) && "(Lorg/geysermc/geyser/api/extension/ExtensionManager;)V".equals(descriptor)) {
                CommonData.LOGGER.info("Found method 'loadAllExtensions', applying transformation...");
                return new LoadAllExtensionsMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class LoadAllExtensionsMethodVisitor extends MethodVisitor {
        public LoadAllExtensionsMethodVisitor(MethodVisitor methodVisitor) {
            super(ASM9, methodVisitor);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                CommonData.LOGGER.info("Injecting emotecraft code...");
                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label catchHandler = new Label();
                Label exit = new Label();
                super.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable");
                super.visitLabel(tryStart);
                super.visitTypeInsn(NEW, "org/redlance/dima_dencep/mods/emotecraft/geyser/EmotecraftExt");
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, "org/redlance/dima_dencep/mods/emotecraft/geyser/EmotecraftExt", "<init>", "()V", false);
                super.visitVarInsn(ASTORE, 2);
                super.visitTypeInsn(NEW, "java/io/InputStreamReader");
                super.visitInsn(DUP);
                super.visitLdcInsn(Type.getType("Lorg/redlance/dima_dencep/mods/emotecraft/geyser/EmotecraftExt;"));
                super.visitLdcInsn("/extension.yml");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
                super.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                super.visitTypeInsn(CHECKCAST, "java/io/InputStream");
                super.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
                super.visitMethodInsn(INVOKESTATIC, "org/geysermc/geyser/extension/GeyserExtensionDescription", "fromYaml", "(Ljava/io/Reader;)Lorg/geysermc/geyser/extension/GeyserExtensionDescription;", false);
                super.visitVarInsn(ASTORE, 3);
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 2);
                super.visitVarInsn(ALOAD, 3);
                super.visitLdcInsn(".");
                super.visitInsn(ICONST_0);
                super.visitTypeInsn(ANEWARRAY, "java/lang/String");
                super.visitMethodInsn(INVOKESTATIC, "java/nio/file/Path", "of", "(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;", true);
                super.visitTypeInsn(NEW, "org/geysermc/geyser/extension/event/GeyserExtensionEventBus");
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESTATIC, "org/geysermc/geyser/GeyserImpl", "getInstance", "()Lorg/geysermc/geyser/GeyserImpl;", false);
                super.visitMethodInsn(INVOKEVIRTUAL, "org/geysermc/geyser/GeyserImpl", "eventBus", "()Lorg/geysermc/geyser/api/event/EventBus;", false);
                super.visitVarInsn(ALOAD, 2);
                super.visitMethodInsn(INVOKESPECIAL, "org/geysermc/geyser/extension/event/GeyserExtensionEventBus", "<init>", "(Lorg/geysermc/geyser/api/event/EventBus;Lorg/geysermc/geyser/api/extension/Extension;)V", false);
                super.visitMethodInsn(INVOKESPECIAL, "org/geysermc/geyser/extension/GeyserExtensionLoader", "setup", "(Lorg/geysermc/geyser/api/extension/Extension;Lorg/geysermc/geyser/extension/GeyserExtensionDescription;Ljava/nio/file/Path;Lorg/geysermc/geyser/api/event/ExtensionEventBus;)Lorg/geysermc/geyser/extension/GeyserExtensionContainer;", false);
                super.visitVarInsn(ASTORE, 4);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "org/geysermc/geyser/extension/GeyserExtensionLoader", "extensionContainers", "Ljava/util/Map;");
                super.visitVarInsn(ALOAD, 2);
                super.visitVarInsn(ALOAD, 4);
                super.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                super.visitInsn(POP);
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 2);
                super.visitVarInsn(ALOAD, 1);
                super.visitMethodInsn(INVOKEVIRTUAL, "org/geysermc/geyser/extension/GeyserExtensionLoader", "register", "(Lorg/geysermc/geyser/api/extension/Extension;Lorg/geysermc/geyser/api/extension/ExtensionManager;)V", false);
                super.visitLabel(tryEnd);
                super.visitJumpInsn(GOTO, exit);
                super.visitLabel(catchHandler);
                super.visitTypeInsn(NEW, "java/lang/RuntimeException");
                super.visitInsn(DUP_X1);
                super.visitInsn(SWAP);
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
                super.visitInsn(ATHROW);
                super.visitLabel(exit);
            }
            super.visitInsn(opcode);
        }
    }
}
