package io.github.kosmx.emotes.common.tools;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    protected final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int available() {
        return this.buffer.remaining();
    }

    @Override
    public int read() throws IOException {
        if (!this.buffer.hasRemaining()) return -1;
        return this.buffer.get() & 0xFF;
    }

    @Override
    public int read(byte @NotNull [] bytes, int off, int len) throws IOException {
        if (!this.buffer.hasRemaining()) return -1;
        len = Math.min(len, this.buffer.remaining());
        this.buffer.get(bytes, off, len);
        return len;
    }

    @Override
    public void close() {
        // no-op
    }
}
