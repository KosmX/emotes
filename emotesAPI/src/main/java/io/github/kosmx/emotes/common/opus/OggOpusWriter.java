package io.github.kosmx.emotes.common.opus;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Muxes Opus packets back into an Ogg stream.
 */
public class OggOpusWriter implements Closeable {
    private static final int PAGE_HEADER_SIZE = 27;
    private static final int MAX_SEGMENTS = 255;

    // The payload is built past the widest possible segment table and moved down once the table is final
    private static final int PAYLOAD_BASE = PAGE_HEADER_SIZE + MAX_SEGMENTS;

    private static final int CONTINUED = 0x01;
    private static final int BOS = 0x02;
    private static final int EOS = 0x04;

    private static final int SERIAL = 0x454D4F54;

    private static final byte[] HEAD_MAGIC = "OpusHead".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] TAGS_MAGIC = "OpusTags".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] VENDOR = "Emotecraft".getBytes(StandardCharsets.UTF_8);

    private static final int[] CRC = new int[256];
    static {
        for (int i = 0; i < CRC.length; i++) {
            int crc = i << 24;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x80000000) != 0 ? (crc << 1) ^ 0x04C11DB7 : crc << 1;
            }
            CRC[i] = crc;
        }
    }

    private final OutputStream out;
    private final byte[] page = new byte[PAYLOAD_BASE + MAX_SEGMENTS * 255];

    private int segmentCount;
    private int payloadLength;
    private int sequence;
    private long granule;
    private int type; // header type of the page being built, not of the one just written

    public OggOpusWriter(OutputStream out, int preSkip, int outputGain, @Nullable Integer trackGain, int loopStart) throws IOException {
        this.out = out;
        writeHead(preSkip, outputGain);
        writeTags(trackGain, loopStart);
    }

    public void writePacket(byte[] packet) throws IOException {
        append(packet);
        this.granule += OpusPackets.sampleCount(packet, OpusPackets.SAMPLE_RATE);
    }

    @Override
    public void close() throws IOException {
        try (OutputStream stream = this.out) {
            this.type |= EOS;
            flush(this.granule);
        }
    }

    private void writeHead(int preSkip, int outputGain) throws IOException {
        byte[] head = new byte[19];
        System.arraycopy(HEAD_MAGIC, 0, head, 0, HEAD_MAGIC.length);
        head[8] = 1;
        head[9] = 1;
        putShort(head, 10, preSkip);
        putInt(head, 12, OpusPackets.SAMPLE_RATE);
        putShort(head, 16, outputGain);
        head[18] = 0; // channel mapping family

        this.type = BOS;
        append(head);
        flush(0);
    }

    private void writeTags(@Nullable Integer trackGain, int loopStart) throws IOException {
        List<byte[]> comments = new ArrayList<>(2);
        if (trackGain != null) comments.add(("R128_TRACK_GAIN=" + trackGain).getBytes(StandardCharsets.UTF_8));
        if (loopStart >= 0) comments.add(("LOOPSTART=" + loopStart).getBytes(StandardCharsets.UTF_8));

        int length = 16 + VENDOR.length;
        for (byte[] comment : comments) length += 4 + comment.length;

        byte[] tags = new byte[length];
        System.arraycopy(TAGS_MAGIC, 0, tags, 0, TAGS_MAGIC.length);
        putInt(tags, 8, VENDOR.length);
        System.arraycopy(VENDOR, 0, tags, 12, VENDOR.length);

        int offset = 12 + VENDOR.length;
        putInt(tags, offset, comments.size());
        offset += 4;

        for (byte[] comment : comments) {
            putInt(tags, offset, comment.length);
            System.arraycopy(comment, 0, tags, offset + 4, comment.length);
            offset += 4 + comment.length;
        }

        append(tags);
        flush(0);
    }

    private void append(byte[] packet) throws IOException {
        int offset = 0;
        int lacing;
        do {
            if (this.segmentCount == MAX_SEGMENTS) {
                // The continued flag belongs on the page that starts mid-packet, not on the one being closed
                flush(this.granule);
                if (offset > 0) this.type = CONTINUED;
            }

            lacing = Math.min(255, packet.length - offset);
            this.page[PAGE_HEADER_SIZE + this.segmentCount++] = (byte) lacing;
            System.arraycopy(packet, offset, this.page, PAYLOAD_BASE + this.payloadLength, lacing);
            this.payloadLength += lacing;
            offset += lacing;
        } while (lacing == 255);
    }

    private void flush(long granule) throws IOException {
        int offset = PAGE_HEADER_SIZE + this.segmentCount;
        System.arraycopy(this.page, PAYLOAD_BASE, this.page, offset, this.payloadLength);

        this.page[0] = 'O';
        this.page[1] = 'g';
        this.page[2] = 'g';
        this.page[3] = 'S';
        this.page[4] = 0;
        this.page[5] = (byte) this.type;
        putLong(this.page, 6, granule);
        putInt(this.page, 14, SERIAL);
        putInt(this.page, 18, this.sequence++);
        putInt(this.page, 22, 0); // the checksum covers the page with its own field zeroed
        this.page[26] = (byte) this.segmentCount;

        int length = offset + this.payloadLength;
        putInt(this.page, 22, crc(this.page, length));
        this.out.write(this.page, 0, length);

        this.segmentCount = 0;
        this.payloadLength = 0;
        this.type = 0;
    }

    private static int crc(byte[] bytes, int length) {
        int crc = 0;
        for (int i = 0; i < length; i++) {
            crc = (crc << 8) ^ CRC[((crc >>> 24) ^ (bytes[i] & 0xFF)) & 0xFF];
        }
        return crc;
    }

    private static void putShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
    }

    private static void putInt(byte[] bytes, int offset, int value) {
        putShort(bytes, offset, value);
        putShort(bytes, offset + 2, value >> 16);
    }

    private static void putLong(byte[] bytes, int offset, long value) {
        putInt(bytes, offset, (int) value);
        putInt(bytes, offset + 4, (int) (value >> 32));
    }
}
