package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
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

    public static byte[] patch(byte[] bytes) {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new ByteArrayClassPath(CLASS_NAME, bytes));

        try {
            CtClass cc = pool.get(CLASS_NAME);

            CtClass protocolProviderInterface = pool.get("org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.ProtocolProvider");
            cc.addInterface(protocolProviderInterface);
            cc.addMethod(CtNewMethod.make("""
                public org.geysermc.mcprotocollib.protocol.MinecraftProtocol ec$protocol() {
                    return this.protocol;
                }""", cc
            ));

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
