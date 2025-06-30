package io.github.kosmx.emotes.bukkit.fuckery;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public class StreamCodecUtils {
    protected static final MethodHandles.Lookup TRUSTED_LOOKUP;

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

    protected static final List<String> FALLBACK_FIELDS = Arrays.asList("val$fallback", "val$fallbackProvider");

    public static void replaceFallback(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec, CustomPacketPayload.FallbackProvider<?> provider) throws ReflectiveOperationException {
        ReflectiveOperationException exception = null;
        for (String fallbackField : FALLBACK_FIELDS) {
            try {
                VarHandle varHandle = TRUSTED_LOOKUP.findVarHandle(codec.getClass(), fallbackField, CustomPacketPayload.FallbackProvider.class);
                varHandle.set(codec, provider);
                return;
            } catch (ReflectiveOperationException ex) {
                exception = ex;
            }
        }
        if (exception != null) {
            CommonData.LOGGER.info(Arrays.toString(codec.getClass().getDeclaredFields()));
            throw exception;
        }
    }

    public static StreamCodec<? extends ByteBuf, ? extends Packet<?>> getThis(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec) throws ReflectiveOperationException {
        try {
            VarHandle varHandle = TRUSTED_LOOKUP.findVarHandle(codec.getClass(), "this$0", StreamCodec.class);
            return (StreamCodec<? extends ByteBuf, ? extends Packet<?>>) varHandle.get(codec);
        } catch (ReflectiveOperationException ex) {
            CommonData.LOGGER.info(Arrays.toString(codec.getClass().getDeclaredFields()));
            throw ex;
        }
    }
}
