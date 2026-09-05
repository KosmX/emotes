package io.github.kosmx.emotes.common.opus;

import java.nio.charset.StandardCharsets;

/**
 * What the reader and the writer have to agree on.
 */
final class OggOpus {
    static final byte[] CAPTURE = "OggS".getBytes(StandardCharsets.US_ASCII);
    static final byte[] HEAD_MAGIC = "OpusHead".getBytes(StandardCharsets.US_ASCII);
    static final byte[] TAGS_MAGIC = "OpusTags".getBytes(StandardCharsets.US_ASCII);

    static final String TRACK_GAIN = "R128_TRACK_GAIN=";
    static final String LOOP_START = "LOOPSTART=";

    // A lacing value below this ends the packet, so a full one means it carries on
    static final int MAX_LACING = 255;

    // The segment count is one byte on the wire, so a page can never describe more than this
    static final int MAX_SEGMENTS = 255;

    static final int CONTINUED = 0x01;
    static final int BOS = 0x02;
    static final int EOS = 0x04;

    // Ogg's own CRC: polynomial 0x04C11DB7, neither reflected nor inverted
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

    static int crc(int crc, int value) {
        return (crc << 8) ^ CRC[((crc >>> 24) ^ (value & 0xFF)) & 0xFF];
    }

    static int crc(int crc, byte[] bytes, int offset, int length) {
        for (int i = offset, end = offset + length; i < end; i++) crc = crc(crc, bytes[i]);
        return crc;
    }

    private OggOpus() {}
}
