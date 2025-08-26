package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class GeyserSessionPatch {
    public static final String CLASS_NAME = "org.geysermc.geyser.session.GeyserSession";

    @SuppressWarnings("unused")
    public static void hook(StartGamePacket packet) {
        System.out.println(packet);
    }

    public static byte[] patch(byte[] bytes) {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new ByteArrayClassPath(CLASS_NAME, bytes));

        try {
            CtClass cc = pool.get(CLASS_NAME);

            CtClass protocolProviderInterface = pool.get("org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.ProtocolProvider");
            cc.addInterface(protocolProviderInterface);

            String methodSrc = """
                public org.geysermc.mcprotocollib.protocol.MinecraftProtocol ec$getProtocol() {
                    return this.protocol;
                }""";
            cc.addMethod(CtNewMethod.make(methodSrc, cc));

            CtMethod startGameMethod = cc.getDeclaredMethod("startGame");
            startGameMethod.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals("org.geysermc.geyser.session.UpstreamSession") && m.getMethodName().equals("sendPacket")) {
                        m.replace("org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.GeyserSessionPatch.hook(startGamePacket); $_ = $proceed($$);");
                    }
                }
            });

            return cc.toBytecode();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to patch!", ex);
        }
    }

    public static void patchClass(Class<?> nearClass, String name, UnaryOperator<byte[]> patcher) throws ReflectiveOperationException, IOException {
        try (InputStream is = Objects.requireNonNull(nearClass.getClassLoader().getResourceAsStream(name))) {
            byte[] bytecode = patcher.apply(is.readAllBytes());
            MethodHandles.privateLookupIn(nearClass, MethodHandles.lookup()).defineClass(bytecode);
        }
    }
}
