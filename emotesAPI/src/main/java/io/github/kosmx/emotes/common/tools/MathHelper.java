package io.github.kosmx.emotes.common.tools;

import io.netty.buffer.ByteBuf;

public class MathHelper {
    public static byte[] readBytes(ByteBuf buf) {
        return readBytes(buf, buf.readableBytes());
    }

    public static byte[] readBytes(ByteBuf buf, int size) {
        byte[] bytes = new byte[size];
        buf.readBytes(bytes);
        return bytes;
    }
}
