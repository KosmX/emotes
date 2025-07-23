package io.github.kosmx.emotes.common.tools;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
    protected final ByteBuffer buffer;

    public ByteBufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.put((byte) b);
    }

    @Override
    public void write(byte @NotNull [] bytes, int off, int len) throws IOException {
        this.buffer.put(bytes, off, len);
    }

    @Override
    public void close() {
        // no-op
    }
}
