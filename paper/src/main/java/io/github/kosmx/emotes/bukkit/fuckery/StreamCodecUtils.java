package io.github.kosmx.emotes.bukkit.fuckery;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.redlance.common.utils.ReflectUtils;

import java.lang.invoke.VarHandle;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class StreamCodecUtils {
    public static void replaceFallback(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec, CustomPacketPayload.FallbackProvider<?> provider) throws ReflectiveOperationException {
        try {
            VarHandle varHandle = ReflectUtils.TRUSTED_LOOKUP.findVarHandle(codec.getClass(), "val$fallback", CustomPacketPayload.FallbackProvider.class);
            varHandle.set(codec, provider);
        } catch (ReflectiveOperationException ex) {
            CommonData.LOGGER.info(Arrays.toString(codec.getClass().getDeclaredFields()));
            throw ex;
        }
    }

    public static StreamCodec<? extends ByteBuf, ? extends Packet<?>> getThis(StreamCodec<? extends ByteBuf, ? extends Packet<?>> codec) throws ReflectiveOperationException {
        try {
            VarHandle varHandle = ReflectUtils.TRUSTED_LOOKUP.findVarHandle(codec.getClass(), "this$0", StreamCodec.class);
            return (StreamCodec<? extends ByteBuf, ? extends Packet<?>>) varHandle.get(codec);
        } catch (ReflectiveOperationException ex) {
            CommonData.LOGGER.info(Arrays.toString(codec.getClass().getDeclaredFields()));
            throw ex;
        }
    }
}
