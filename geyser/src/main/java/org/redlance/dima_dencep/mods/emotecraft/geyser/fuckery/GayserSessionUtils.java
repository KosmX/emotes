package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

@SuppressWarnings("deprecation")
public class GayserSessionUtils {
    private static final MethodHandles.Lookup TRUSTED_LOOKUP;

    static {
        try {
            Field hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            TRUSTED_LOOKUP = (MethodHandles.Lookup) UnsafeAccess.UNSAFE.getObject(
                    UnsafeAccess.UNSAFE.staticFieldBase(hackfield),
                    UnsafeAccess.UNSAFE.staticFieldOffset(hackfield)
            );
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final VarHandle PROTOCOL = uncheck(() ->
            TRUSTED_LOOKUP.findVarHandle(GeyserSession.class, "protocol", MinecraftProtocol.class)
    );

    public static MinecraftProtocol getProtocol(GeyserSession session) {
        return (MinecraftProtocol) PROTOCOL.get(session);
    }

    public static <R, E extends Exception> R uncheck(Supplier_WithExceptions<R, E> supplier) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @FunctionalInterface
    public interface Supplier_WithExceptions<T, E extends Exception> {
        T get() throws E;
    }
}
