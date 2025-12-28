package io.github.kosmx.emotes.common.tools;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MathHelper {
    public static ByteBuffer readFromIStream(InputStream stream) throws IOException {
        return ByteBuffer.wrap(stream.readAllBytes());
    }

    /**
     * If {@link ByteBuffer} is wrapped, it is safe to get the array
     * but if is direct manual read is required.
     * @param byteBuffer get the bytes from
     * @return the byte array
     */
    public static byte[] safeGetBytesFromBuffer(ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect() || byteBuffer.isReadOnly()) {
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            return bytes;
        }
        else return byteBuffer.array();
    }

    public static byte[] readBytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }
}
