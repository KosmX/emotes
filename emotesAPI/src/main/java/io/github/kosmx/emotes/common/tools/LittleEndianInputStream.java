package io.github.kosmx.emotes.common.tools;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Little-endian primitives off a stream. Holds no state.
 */
public class LittleEndianInputStream extends FilterInputStream {
    public LittleEndianInputStream(InputStream in) {
        super(in);
    }

    public int readUnsignedByte() throws IOException {
        int value = read();
        if (value < 0) throw new EOFException();
        return value;
    }

    public int readUnsignedShort() throws IOException {
        return readUnsignedByte() | (readUnsignedByte() << 8);
    }

    public int readInt() throws IOException {
        return readUnsignedShort() | (readUnsignedShort() << 16);
    }

    /**
     * Fills {@code length} bytes or throws.
     */
    public void readBytes(byte[] bytes, int offset, int length) throws IOException {
        int done = 0;
        while (done < length) {
            int count = read(bytes, offset + done, length - done);
            if (count < 0) throw new EOFException();
            done += count;
        }
    }
}
