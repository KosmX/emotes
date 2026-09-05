package io.github.kosmx.emotes.common.opus;

import io.github.kosmx.emotes.common.tools.LittleEndianInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Pulls Opus packets out of an Ogg stream. Page boundaries and packet boundaries are unrelated:
 * a packet is a run of segments ending at the first one shorter than 255, and it may cross pages.
 */
public class OggOpusReader extends LittleEndianInputStream {
    // Longer comments are skipped unread, which is how album art stays out of memory
    private static final int MAX_COMMENT = 128;

    private final byte[] table = new byte[OggOpus.MAX_SEGMENTS];
    private int segmentCount;
    private int segmentIndex;
    private int chunkLeft;

    private int serial;
    private int sequence;
    private boolean serialKnown;
    private boolean ended;

    private final byte[] header = new byte[27];
    private int crc;
    private int expectedCrc;
    private boolean checking;

    private int channelCount;
    private int preSkip;
    private int outputGain;
    @Nullable
    private Integer trackGain;
    @Nullable
    private Integer loopStart;

    public OggOpusReader(InputStream in) throws IOException {
        super(in);
        try {
            readHead();
            readTags();
        } catch (EOFException e) {
            throw new OpusFormatException("Opus headers end early");
        }
    }

    /**
     * Rewinding the bytes would leave the page and packet state pointing at the wrong ones.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int limit) {
        // Refused through markSupported, and mark itself is documented to do nothing when unsupported
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("An Opus reader cannot be rewound");
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
    public byte @Nullable [] readPacket() throws IOException {
        try {
            return nextPacket();
        } catch (EOFException e) {
            throw new OpusFormatException("Ogg page ends early");
        }
    }

    private byte @Nullable [] nextPacket() throws IOException {
        byte[] packet = null;

        while (true) {
            if (this.segmentIndex == this.segmentCount) {
                int type = nextPage();
                if (type < 0) {
                    if (packet != null) throw new OpusFormatException("Truncated Opus packet");
                    return null;
                }
                // The flag has to say exactly what we are in the middle of, either way round
                if ((packet != null) != ((type & OggOpus.CONTINUED) != 0)) {
                    throw new OpusFormatException("Lost packet continuation");
                }
            }

            int chunk = walkPacket();
            if (chunk > 0) {
                int offset = packet == null ? 0 : packet.length;
                packet = packet == null ? new byte[chunk] : Arrays.copyOf(packet, offset + chunk);
                payloadRead(packet, offset, chunk);
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
            if (lacing != OggOpus.MAX_LACING) break;
        }
        return length;
    }

    private boolean packetEnded() {
        return this.segmentIndex > 0 && (this.table[this.segmentIndex - 1] & 0xFF) != OggOpus.MAX_LACING;
    }

    private static int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    /**
     * A page is only whole once its payload has been read, so this runs before moving to the next one.
     */
    private void verifyPage() throws IOException {
        if (!this.checking) return;

        this.checking = false;
        if (this.crc != this.expectedCrc) throw new OpusFormatException("Ogg page is corrupt");
    }

    private int payloadByte() throws IOException {
        int value = readUnsignedByte();
        this.crc = OggOpus.crc(this.crc, value);
        return value;
    }

    private void payloadRead(byte[] bytes, int offset, int length) throws IOException {
        readBytes(bytes, offset, length);
        this.crc = OggOpus.crc(this.crc, bytes, offset, length);
    }

    private void payloadSkip(int length) throws IOException {
        for (int i = 0; i < length; i++) payloadByte();
    }

    /**
     * @return the header type of the next page belonging to our stream, or -1 at end of stream
     */
    private int nextPage() throws IOException {
        // Whatever follows the page the stream said was its last is not ours to read
        if (this.ended) {
            verifyPage(); // nothing else will come along to check the last one
            return -1;
        }

        while (true) {
            verifyPage(); // the one we are leaving is only complete now

            int capture = read();
            if (capture < 0) {
                throw new OpusFormatException("Ogg stream ends without an end-of-stream page");
            }

            this.header[0] = (byte) capture;
            readBytes(this.header, 1, this.header.length - 1);

            for (int i = 0; i < OggOpus.CAPTURE.length; i++) {
                if (this.header[i] != OggOpus.CAPTURE[i]) throw new OpusFormatException("Not an Ogg page");
            }
            if (this.header[4] != 0) throw new OpusFormatException("Unsupported Ogg version");

            int type = this.header[5] & 0xFF;
            int serial = readInt(this.header, 14);
            int sequence = readInt(this.header, 18);
            this.expectedCrc = readInt(this.header, 22);

            // The checksum is taken over the page with its own field zeroed
            Arrays.fill(this.header, 22, 26, (byte) 0);
            this.crc = OggOpus.crc(0, this.header, 0, this.header.length);

            this.segmentCount = this.header[26] & 0xFF;
            this.segmentIndex = 0;
            readBytes(this.table, 0, this.segmentCount);
            this.crc = OggOpus.crc(this.crc, this.table, 0, this.segmentCount);
            this.checking = true;

            if (!this.serialKnown) {
                if ((type & OggOpus.BOS) == 0) throw new OpusFormatException("Ogg stream does not begin with a header page");
                this.serial = serial;
                this.sequence = sequence;
                this.serialKnown = true;
            } else if (serial == this.serial && sequence != ++this.sequence) {
                throw new OpusFormatException("Ogg pages are out of order at " + sequence);
            }

            if (serial == this.serial) {
                this.ended = (type & OggOpus.EOS) != 0;
                return type;
            }

            skipPage(); // another logical stream is multiplexed in
        }
    }

    private void skipPage() throws IOException {
        this.checking = false; // a page we never read cannot be checked
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
        if (type < 0 || (type & OggOpus.CONTINUED) == 0) throw new OpusFormatException("Lost packet continuation");
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
        return payloadByte();
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
            payloadRead(bytes, offset, count);
            this.chunkLeft -= count;
            offset += count;
            length -= count;
        }
    }

    private void packetSkip(int length) throws IOException {
        while (length > 0) {
            while (this.chunkLeft == 0) nextChunk();

            int count = Math.min(length, this.chunkLeft);
            payloadSkip(count);
            this.chunkLeft -= count;
            length -= count;
        }
    }

    private void endPacket() throws IOException {
        payloadSkip(this.chunkLeft);
        this.chunkLeft = 0;

        while (!packetEnded()) {
            nextChunk();
            payloadSkip(this.chunkLeft);
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
        expect(OggOpus.HEAD_MAGIC, "OpusHead");

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
        expect(OggOpus.TAGS_MAGIC, "OpusTags");

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
        if (this.trackGain == null && comment.regionMatches(true, 0, OggOpus.TRACK_GAIN, 0, OggOpus.TRACK_GAIN.length())) {
            this.trackGain = number(comment.substring(OggOpus.TRACK_GAIN.length()));
        } else if (this.loopStart == null && comment.regionMatches(true, 0, OggOpus.LOOP_START, 0, OggOpus.LOOP_START.length())) {
            this.loopStart = number(comment.substring(OggOpus.LOOP_START.length()));
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
