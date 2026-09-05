package io.github.kosmx.emotes.common.opus;

import io.github.kosmx.emotes.common.tools.LittleEndianInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Pulls Opus packets out of an Ogg stream. Page boundaries and packet boundaries are unrelated:
 * a packet is a run of segments ending at the first one shorter than 255, and it may cross pages.
 */
public class OggOpusReader extends LittleEndianInputStream {
    private static final int CONTINUED = 0x01;
    private static final int BOS = 0x02;

    private static final byte[] HEAD_MAGIC = "OpusHead".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] TAGS_MAGIC = "OpusTags".getBytes(StandardCharsets.US_ASCII);
    private static final String TRACK_GAIN = "R128_TRACK_GAIN=";
    private static final String LOOP_START = "LOOPSTART=";

    // Longer comments are skipped unread, which is how album art stays out of memory
    private static final int MAX_COMMENT = 128;

    private final byte[] table = new byte[255];
    private int segmentCount;
    private int segmentIndex;
    private int chunkLeft;

    private int serial;
    private boolean serialKnown;

    private int channelCount;
    private int preSkip;
    private int outputGain;
    @Nullable
    private Integer trackGain;
    @Nullable
    private Integer loopStart;

    public OggOpusReader(InputStream in) throws IOException {
        super(in);
        readHead();
        readTags();
    }

    public int channelCount() {
        return this.channelCount;
    }

    public int preSkip() {
        return this.preSkip;
    }

    public int outputGain() {
        return this.outputGain;
    }

    @Nullable
    public Integer trackGain() {
        return this.trackGain;
    }

    /**
     * @return the {@code LOOPSTART} tag in samples, null when the track is not meant to loop
     */
    @Nullable
    public Integer loopStart() {
        return this.loopStart;
    }

    /**
     * @return the next audio packet, or null once the stream is over
     */
    @Nullable
    public byte[] readPacket() throws IOException {
        byte[] packet = null;

        while (true) {
            if (this.segmentIndex == this.segmentCount) {
                int type = nextPage();
                if (type < 0) {
                    if (packet != null) throw new OpusFormatException("Truncated Opus packet");
                    return null;
                }
                if (packet != null && (type & CONTINUED) == 0) throw new OpusFormatException("Lost packet continuation");
            }

            int chunk = walkPacket();
            if (chunk > 0) {
                int offset = packet == null ? 0 : packet.length;
                packet = packet == null ? new byte[chunk] : Arrays.copyOf(packet, offset + chunk);
                readBytes(packet, offset, chunk);
            }
            if (packetEnded()) return packet == null ? new byte[0] : packet;
        }
    }

    /**
     * @return how many bytes of the current packet live on this page
     */
    private int walkPacket() {
        int length = 0;
        while (this.segmentIndex < this.segmentCount) {
            int lacing = this.table[this.segmentIndex++] & 0xFF;
            length += lacing;
            if (lacing != 255) break;
        }
        return length;
    }

    private boolean packetEnded() {
        return this.segmentIndex > 0 && (this.table[this.segmentIndex - 1] & 0xFF) != 255;
    }

    /**
     * @return the header type of the next page belonging to our stream, or -1 at end of stream
     */
    private int nextPage() throws IOException {
        while (true) {
            int capture = read();
            if (capture < 0) return -1;
            if (capture != 'O' || readUnsignedByte() != 'g' || readUnsignedByte() != 'g' || readUnsignedByte() != 'S') {
                throw new OpusFormatException("Not an Ogg page");
            }
            if (readUnsignedByte() != 0) throw new OpusFormatException("Unsupported Ogg version");

            int type = readUnsignedByte();
            skipNBytes(8); // granule position
            int serial = readInt();
            skipNBytes(8); // page sequence and checksum

            this.segmentCount = readUnsignedByte();
            this.segmentIndex = 0;
            readBytes(this.table, 0, this.segmentCount);

            if (!this.serialKnown) {
                if ((type & BOS) == 0) throw new OpusFormatException("Ogg stream does not begin with a header page");
                this.serial = serial;
                this.serialKnown = true;
            }
            if (serial == this.serial) return type;

            skipPage(); // another logical stream is multiplexed in
        }
    }

    private void skipPage() throws IOException {
        int length = 0;
        for (int i = 0; i < this.segmentCount; i++) length += this.table[i] & 0xFF;
        skipNBytes(length);
        this.segmentCount = 0;
    }

    private boolean startPacket() throws IOException {
        while (this.segmentIndex == this.segmentCount) {
            if (nextPage() < 0) return false;
        }
        this.chunkLeft = walkPacket();
        return true;
    }

    private void nextChunk() throws IOException {
        if (packetEnded()) throw new OpusFormatException("Opus header ends early");

        int type = nextPage();
        if (type < 0 || (type & CONTINUED) == 0) throw new OpusFormatException("Lost packet continuation");
        this.chunkLeft = walkPacket();
    }

    private boolean hasPacketBytes() throws IOException {
        while (this.chunkLeft == 0) {
            if (packetEnded()) return false;
            nextChunk();
        }
        return true;
    }

    private int packetByte() throws IOException {
        while (this.chunkLeft == 0) nextChunk();
        this.chunkLeft--;
        return readUnsignedByte();
    }

    private int packetShort() throws IOException {
        return packetByte() | (packetByte() << 8);
    }

    private int packetInt() throws IOException {
        return packetShort() | (packetShort() << 16);
    }

    private void packetRead(byte[] bytes, int offset, int length) throws IOException {
        while (length > 0) {
            while (this.chunkLeft == 0) nextChunk();

            int count = Math.min(length, this.chunkLeft);
            readBytes(bytes, offset, count);
            this.chunkLeft -= count;
            offset += count;
            length -= count;
        }
    }

    private void packetSkip(int length) throws IOException {
        while (length > 0) {
            while (this.chunkLeft == 0) nextChunk();

            int count = Math.min(length, this.chunkLeft);
            skipNBytes(count);
            this.chunkLeft -= count;
            length -= count;
        }
    }

    private void endPacket() throws IOException {
        skipNBytes(this.chunkLeft);
        this.chunkLeft = 0;

        while (!packetEnded()) {
            nextChunk();
            skipNBytes(this.chunkLeft);
            this.chunkLeft = 0;
        }
    }

    private void expect(byte[] magic, String name) throws IOException {
        for (byte b : magic) {
            if (packetByte() != (b & 0xFF)) throw new OpusFormatException("Opus stream has no " + name + " header");
        }
    }

    private void readHead() throws IOException {
        if (!startPacket()) throw new OpusFormatException("Empty Ogg stream");
        expect(HEAD_MAGIC, "OpusHead");

        if (packetByte() >> 4 != 0) throw new OpusFormatException("Unsupported OpusHead version");
        this.channelCount = packetByte();
        this.preSkip = packetShort();
        packetSkip(4); // original sample rate, informational only
        this.outputGain = (short) packetShort();
        if (packetByte() != 0) throw new OpusFormatException("Unsupported channel mapping family");
        endPacket();
    }

    private void readTags() throws IOException {
        if (!startPacket()) throw new OpusFormatException("Opus stream has no OpusTags header");
        expect(TAGS_MAGIC, "OpusTags");

        packetSkip(commentLength()); // vendor string

        int count = commentLength();
        for (int i = 0; i < count && hasPacketBytes(); i++) {
            int length = commentLength();
            if (length > MAX_COMMENT) {
                packetSkip(length);
                continue;
            }

            byte[] comment = new byte[length];
            packetRead(comment, 0, length);
            readComment(new String(comment, StandardCharsets.UTF_8));
        }
        endPacket();
    }

    private int commentLength() throws IOException {
        int length = packetInt();
        if (length < 0) throw new OpusFormatException("Malformed OpusTags header");
        return length;
    }

    private void readComment(String comment) {
        if (this.trackGain == null && comment.regionMatches(true, 0, TRACK_GAIN, 0, TRACK_GAIN.length())) {
            this.trackGain = number(comment.substring(TRACK_GAIN.length()));
        } else if (this.loopStart == null && comment.regionMatches(true, 0, LOOP_START, 0, LOOP_START.length())) {
            this.loopStart = number(comment.substring(LOOP_START.length()));
        }
    }

    @Nullable
    private static Integer number(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
