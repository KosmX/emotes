package io.github.kosmx.emotes.common.opus;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Muxes Opus packets back into an Ogg stream.
 */
public class OggOpusWriter implements Closeable {
    private static final int PAGE_HEADER_SIZE = 27;

    // How full a page is allowed to get, which the format caps but does not dictate
    private static final int SEGMENTS_PER_PAGE = OggOpus.MAX_SEGMENTS;

    // The payload is built past the widest possible segment table and moved down once the table is final
    private static final int PAYLOAD_BASE = PAGE_HEADER_SIZE + SEGMENTS_PER_PAGE;

    private static final byte[] VENDOR = "Emotecraft".getBytes(StandardCharsets.UTF_8);

    private final OutputStream out;
    private final byte[] page = new byte[PAYLOAD_BASE + SEGMENTS_PER_PAGE * OggOpus.MAX_LACING];
    private final int serial = ThreadLocalRandom.current().nextInt();

    private int segmentCount;
    private int payloadLength;
    private int sequence;
    private long granule;
    private int type; // header type of the page being built, not of the one just written
    private boolean closed;

    public OggOpusWriter(OutputStream out, int channelCount, int preSkip, int outputGain,
                         @Nullable Integer trackGain, int loopStart) throws IOException {
        this.out = out;
        writeHead(channelCount, preSkip, outputGain);
        writeTags(trackGain, loopStart);
    }

    public void writePacket(byte[] data, int offset, int length) throws IOException {
        int samples = OpusPackets.sampleCount(data, offset, length, OpusPackets.SAMPLE_RATE);
        // A granule position that went backwards would make the whole stream unseekable
        if (samples <= 0) throw new OpusFormatException("Refusing to write a malformed Opus packet");

        append(data, offset, length);
        this.granule += samples;
    }

    @Override
    public void close() throws IOException {
        if (this.closed) return;
        this.closed = true;

        try (OutputStream stream = this.out) {
            this.type |= OggOpus.EOS;
            flush(this.granule);
        }
    }

    private void writeHead(int channelCount, int preSkip, int outputGain) throws IOException {
        byte[] head = new byte[19];
        System.arraycopy(OggOpus.HEAD_MAGIC, 0, head, 0, OggOpus.HEAD_MAGIC.length);
        head[8] = 1; // OpusHead version
        head[9] = (byte) channelCount;
        putShort(head, 10, preSkip);
        putInt(head, 12, OpusPackets.SAMPLE_RATE);
        putShort(head, 16, outputGain);
        head[18] = 0; // channel mapping family

        this.type = OggOpus.BOS;
        append(head, 0, head.length);
        flush(0);
    }

    private void writeTags(@Nullable Integer trackGain, int loopStart) throws IOException {
        List<byte[]> comments = new ArrayList<>(2);
        if (trackGain != null) comments.add((OggOpus.TRACK_GAIN + trackGain).getBytes(StandardCharsets.UTF_8));
        if (loopStart >= 0) comments.add((OggOpus.LOOP_START + loopStart).getBytes(StandardCharsets.UTF_8));

        int length = 16 + VENDOR.length;
        for (byte[] comment : comments) length += 4 + comment.length;

        byte[] tags = new byte[length];
        System.arraycopy(OggOpus.TAGS_MAGIC, 0, tags, 0, OggOpus.TAGS_MAGIC.length);
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

        append(tags, 0, tags.length);
        flush(0);
    }

    private void append(byte[] data, int offset, int length) throws IOException {
        // A bad range would be written into the segment table before the copy noticed it
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IndexOutOfBoundsException("Packet range " + offset + " + " + length + " of " + data.length);
        }

        int done = 0;
        int lacing;
        do {
            if (this.segmentCount == SEGMENTS_PER_PAGE) {
                // The continued flag belongs on the page that starts mid-packet, not on the one being closed
                flush(this.granule);
                if (done > 0) this.type = OggOpus.CONTINUED;
            }

            lacing = Math.min(OggOpus.MAX_LACING, length - done);
            this.page[PAGE_HEADER_SIZE + this.segmentCount++] = (byte) lacing;
            System.arraycopy(data, offset + done, this.page, PAYLOAD_BASE + this.payloadLength, lacing);
            this.payloadLength += lacing;
            done += lacing;
        } while (lacing == OggOpus.MAX_LACING);
    }

    private void flush(long granule) throws IOException {
        int offset = PAGE_HEADER_SIZE + this.segmentCount;
        System.arraycopy(this.page, PAYLOAD_BASE, this.page, offset, this.payloadLength);

        System.arraycopy(OggOpus.CAPTURE, 0, this.page, 0, OggOpus.CAPTURE.length);
        this.page[4] = 0;
        this.page[5] = (byte) this.type;
        putLong(this.page, 6, granule);
        putInt(this.page, 14, this.serial);
        putInt(this.page, 18, this.sequence++);
        putInt(this.page, 22, 0); // the checksum covers the page with its own field zeroed
        this.page[26] = (byte) this.segmentCount;

        int length = offset + this.payloadLength;
        putInt(this.page, 22, OggOpus.crc(0, this.page, 0, length));
        this.out.write(this.page, 0, length);

        this.segmentCount = 0;
        this.payloadLength = 0;
        this.type = 0;
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
