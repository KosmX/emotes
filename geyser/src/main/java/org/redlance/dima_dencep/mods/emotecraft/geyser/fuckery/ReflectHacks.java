package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import static io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess.UNSAFE;

public class ReflectHacks {
    @SuppressWarnings("removal")
    protected static final MethodHandles.Lookup TRUSTED_LOOKUP = uncheck(() -> {
        Field hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        return (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(hackfield), UNSAFE.staticFieldOffset(hackfield));
    });

    private static final VarHandle GEYSER_SESSION_PROTOCOL = ReflectHacks.uncheck(() -> ReflectHacks.TRUSTED_LOOKUP.findVarHandle(
            GeyserSession.class, "protocol", MinecraftProtocol.class
    ));

    public static MinecraftProtocol getProtocol(GeyserSession session) {
        return (MinecraftProtocol) GEYSER_SESSION_PROTOCOL.get(session);
    }

    @FunctionalInterface
    public interface Supplier_WithExceptions<T, E extends Exception> {
        T get() throws E;
    }

    public static <R, E extends Exception> R uncheck(Supplier_WithExceptions<R, E> supplier) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
