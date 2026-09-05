package io.github.kosmx.emotes.common.opus;

import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.kosmx.emotes.common.CommonData;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A validated mono Opus stream: its packets, and the PCM they decode to once someone asks.
 * The packets live back to back in one array, indexed by {@link #offset} and {@link #length}.
 */
public class OpusSound {
    public static final int NO_LOOP = -1;

    private static final int MAX_DURATION_MS = 10 * 60 * 1000;
    private static final int MAX_SAMPLES = MAX_DURATION_MS * (OpusPackets.SAMPLE_RATE / 1000);

    // Mono Opus gains nothing above this, and a long track has to stay thinner still to fit the packet
    private static final int MAX_BITRATE = 96000;

    // Volume is clamped to [0, 1] by the sound engine, so normalizing can only attenuate loud tracks
    private static final double TARGET_LUFS = -14.0;
    private static final double R128_REFERENCE_LUFS = -23.0;
    private static final double Q7_8 = 256.0;

    // Concentus is fixed-point and does not chew through minutes of audio instantly
    private static final ExecutorService DECODER = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 4), runnable -> {
                Thread thread = new Thread(runnable, "Emotecraft Opus decoder");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
    );

    public record DecodedSound(short[] samples, float normalization) {}

    private final int preSkip;
    private final int outputGain;
    @Nullable
    private final Integer trackGain;
    private final int loopStart;

    private final byte[] data;
    private final int[] offsets; // one past the last packet, so a length is the gap to the next entry
    private final int sampleCount;

    // Minutes of PCM are worth keeping for a replay, but not worth an OutOfMemoryError
    private volatile SoftReference<DecodedSound> pcm = new SoftReference<>(null);
    // Held strongly while a decode runs, so a cleared reference cannot start a second one
    @Nullable
    private volatile CompletableFuture<DecodedSound> decoding;
    private volatile boolean failed;

    public OpusSound(int preSkip, int outputGain, @Nullable Integer trackGain, @Nullable Integer loopStart,
                     byte[] data, int[] offsets) throws OpusFormatException {
        if (offsets.length < 2 || offsets[offsets.length - 1] > data.length) {
            throw new OpusFormatException("Opus stream has no packets");
        }

        long sampleCount = 0;
        long size = Short.BYTES + varIntSize(offsets.length - 1) + Integer.BYTES; // preSkip, count, loop start

        for (int i = 0; i < offsets.length - 1; i++) {
            int length = offsets[i + 1] - offsets[i];
            int samples = OpusPackets.sampleCount(data, offsets[i], length, OpusPackets.SAMPLE_RATE);
            if (samples <= 0) throw new OpusFormatException("Malformed Opus packet");

            // The header says mono, and only the packets themselves can contradict it
            if (OpusPackets.stereo(data[offsets[i]])) throw new OpusFormatException("Opus stream must be mono");

            sampleCount += samples;
            if (sampleCount > MAX_SAMPLES) {
                throw new OpusFormatException("Opus stream is longer than " + MAX_DURATION_MS + " ms");
            }

            size += varIntSize(length) + length;
            if (size > CommonData.MAX_PACKET_SIZE) {
                throw new OpusFormatException("Opus stream is bigger than " + CommonData.MAX_PACKET_SIZE + " bytes");
            }
        }

        if (sampleCount <= preSkip) throw new OpusFormatException("Opus stream has no audio");

        long bitrate = size * 8L * OpusPackets.SAMPLE_RATE / sampleCount;
        long limit = Math.min(MAX_BITRATE, CommonData.MAX_PACKET_SIZE * 8L * OpusPackets.SAMPLE_RATE / sampleCount);
        if (bitrate > limit) {
            throw new OpusFormatException("Opus stream is " + bitrate + " bps, over the " + limit + " bps limit");
        }

        this.preSkip = preSkip;
        this.outputGain = outputGain;
        this.trackGain = trackGain;
        this.data = data;
        this.offsets = offsets;
        this.sampleCount = (int) sampleCount;
        this.loopStart = loopStart != null && loopStart >= 0 && loopStart < this.sampleCount - preSkip
                ? loopStart : NO_LOOP;
    }

    public static OpusSound read(Path file) throws IOException {
        if (Files.size(file) > CommonData.MAX_PACKET_SIZE) throw new OpusFormatException("Opus file is too big to send");

        try (InputStream stream = Files.newInputStream(file)) {
            return read(stream);
        }
    }

    /**
     * Reads without closing the stream, for sounds that come from somewhere other than a file.
     */
    public static OpusSound read(InputStream input) throws IOException {
        OggOpusReader reader = new OggOpusReader(new BufferedInputStream(input));
        if (reader.channelCount() != 1) throw new OpusFormatException("Opus stream must be mono");

        byte[] data = new byte[8192];
        int[] offsets = new int[64];
        int count = 0;
        int length = 0;

        for (byte[] packet = reader.readPacket(); packet != null; packet = reader.readPacket()) {
            // The constructor bounds this too, but only once the whole stream is already in memory
            if (length + packet.length > CommonData.MAX_PACKET_SIZE) {
                throw new OpusFormatException("Opus stream is bigger than " + CommonData.MAX_PACKET_SIZE + " bytes");
            }

            if (count + 1 == offsets.length) offsets = Arrays.copyOf(offsets, offsets.length * 2);
            if (length + packet.length > data.length) {
                data = Arrays.copyOf(data, Math.max(data.length * 2, length + packet.length));
            }

            offsets[count++] = length;
            System.arraycopy(packet, 0, data, length, packet.length);
            length += packet.length;
        }
        offsets[count] = length;

        return new OpusSound(reader.preSkip(), reader.outputGain(), reader.trackGain(), reader.loopStart(),
                Arrays.copyOf(data, length), Arrays.copyOf(offsets, count + 1));
    }

    public void write(Path file) throws IOException {
        // The writer's constructor already writes headers, so the stream needs closing even if that throws
        try (OutputStream stream = Files.newOutputStream(file);
             OggOpusWriter writer = new OggOpusWriter(stream, 1, this.preSkip, this.outputGain, this.trackGain, this.loopStart)) {
            for (int i = 0, count = packetCount(); i < count; i++) {
                writer.writePacket(this.data, this.offsets[i], length(i));
            }
        }
    }

    public int preSkip() {
        return this.preSkip;
    }

    /**
     * @return where a repeat starts, in samples, or {@link #NO_LOOP}
     */
    public int loopStart() {
        return this.loopStart;
    }

    public int packetCount() {
        return this.offsets.length - 1;
    }

    public byte[] data() {
        return this.data;
    }

    public int offset(int index) {
        return this.offsets[index];
    }

    public int length(int index) {
        return this.offsets[index + 1] - this.offsets[index];
    }

    public int durationMs() {
        return (this.sampleCount - this.preSkip) / (OpusPackets.SAMPLE_RATE / 1000);
    }

    /**
     * Decodes in the background if that has not happened yet, or if the result was reclaimed.
     */
    public void startDecoding() {
        if (this.failed || this.decoding != null || this.pcm.get() != null) return;

        synchronized (this) {
            if (this.failed || this.decoding != null || this.pcm.get() != null) return;

            // Store before completing, or an inline finish would clear the field and be overwritten
            CompletableFuture<DecodedSound> decoding = CompletableFuture.supplyAsync(this::decode, DECODER);
            this.decoding = decoding;
            decoding.whenComplete(this::finish);
        }
    }

    private void finish(@Nullable DecodedSound decoded, @Nullable Throwable error) {
        synchronized (this) {
            if (decoded != null) {
                this.pcm = new SoftReference<>(decoded);
            } else {
                this.failed = true; // one bad stream should not be retried on every frame
                CommonData.LOGGER.error("Failed to decode an emote sound", error);
            }
            this.decoding = null;
        }
    }

    /**
     * @return the PCM if it is already decoded, null while it is still being worked on or if it failed
     */
    @Nullable
    public DecodedSound decoded() {
        DecodedSound decoded = this.pcm.get();
        if (decoded == null) startDecoding();
        return decoded;
    }

    private DecodedSound decode() {
        short[] samples = new short[this.sampleCount - this.preSkip];
        Loudness loudness = this.trackGain == null ? new Loudness(samples.length) : null;
        float gain = (float) Math.pow(10.0, this.outputGain / Q7_8 / 20.0);

        try {
            OpusDecoder decoder = new OpusDecoder(OpusPackets.SAMPLE_RATE, 1);
            short[] scratch = new short[OpusPackets.SAMPLE_RATE / 1000 * OpusPackets.MAX_PACKET_DURATION_MS];
            int skip = this.preSkip;
            int offset = 0;

            for (int i = 0, packets = packetCount(); i < packets; i++) {
                int from = this.offsets[i];
                int size = length(i);

                int count;
                if (skip > 0) {
                    // The pre-skip usually cuts a packet in half, so decode it aside and keep the tail
                    int decoded = decoder.decode(this.data, from, size, scratch, 0, scratch.length, false);
                    int dropped = Math.min(decoded, skip);
                    // Trust the decoder over the sample count derived from the TOC bytes
                    count = Math.min(decoded - dropped, samples.length - offset);
                    System.arraycopy(scratch, dropped, samples, offset, count);
                    skip -= dropped;
                } else {
                    count = decoder.decode(this.data, from, size, samples, offset, samples.length - offset, false);
                }

                applyGain(samples, offset, count, gain);
                if (loudness != null) loudness.feed(samples, offset, count);
                offset += count;
            }
        } catch (Exception e) { // Concentus throws IllegalArgumentException as readily as OpusException
            throw new CompletionException(e);
        }

        return new DecodedSound(samples, normalization(loudness));
    }

    private float normalization(@Nullable Loudness loudness) {
        double lufs;
        if (this.trackGain != null) {
            lufs = R128_REFERENCE_LUFS - this.trackGain / Q7_8;
        } else if (loudness != null) {
            lufs = loudness.integrated();
        } else {
            return 1.0F;
        }

        if (!Double.isFinite(lufs)) return 1.0F;
        return (float) Math.min(1.0, Math.pow(10.0, (TARGET_LUFS - lufs) / 20.0));
    }

    private static int varIntSize(int value) {
        return (31 - Integer.numberOfLeadingZeros(value | 1)) / 7 + 1;
    }

    private static void applyGain(short[] samples, int offset, int length, float gain) {
        if (gain == 1.0F) return;

        for (int i = offset, end = offset + length; i < end; i++) {
            samples[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(samples[i] * gain)));
        }
    }
}
