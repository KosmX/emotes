package io.github.kosmx.emotes.common.opus;

/**
 * TOC byte helpers, ported from libopus {@code opus.c}.
 */
public final class OpusPackets {
    public static final int SAMPLE_RATE = 48000;
    public static final int MAX_PACKET_DURATION_MS = 120;

    private OpusPackets() {}

    /**
     * {@code opus_packet_get_samples_per_frame}
     */
    public static int samplesPerFrame(byte toc, int sampleRate) {
        if ((toc & 0x80) != 0) { // CELT-only: 2.5, 5, 10 or 20 ms
            return (sampleRate << ((toc >> 3) & 0x3)) / 400;
        }
        if ((toc & 0x60) == 0x60) { // hybrid: 10 or 20 ms
            return (toc & 0x08) != 0 ? sampleRate / 50 : sampleRate / 100;
        }

        int size = (toc >> 3) & 0x3; // SILK-only: 10, 20, 40 or 60 ms
        return size == 3 ? sampleRate * 60 / 1000 : (sampleRate << size) / 100;
    }

    /**
     * {@code opus_packet_get_nb_frames}, or -1 if malformed.
     */
    public static int frameCount(byte[] packet) {
        if (packet.length < 1) return -1;

        int code = packet[0] & 0x3;
        if (code == 0) return 1;
        if (code != 3) return 2;

        if (packet.length < 2) return -1;
        return packet[1] & 0x3F;
    }

    /**
     * {@code opus_packet_get_nb_samples}, or -1 if malformed.
     */
    public static int sampleCount(byte[] packet, int sampleRate) {
        int frames = frameCount(packet);
        if (frames < 1) return -1;

        int samples = frames * samplesPerFrame(packet[0], sampleRate);
        if (samples * 1000L > (long) sampleRate * MAX_PACKET_DURATION_MS) return -1;
        return samples;
    }
}
