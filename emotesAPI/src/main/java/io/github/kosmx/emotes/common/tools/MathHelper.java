package io.github.kosmx.emotes.common.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MathHelper {
    public static ByteBuffer readFromIStream(InputStream stream) throws IOException {
        return ByteBuffer.wrap(stream.readAllBytes());
    }
}
