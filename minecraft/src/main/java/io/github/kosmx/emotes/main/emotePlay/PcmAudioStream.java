package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.opus.OpusPackets;
import io.github.kosmx.emotes.common.opus.OpusSound;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Hands already decoded PCM to the sound engine in the chunks it asks for.
 */
public class PcmAudioStream implements AudioStream {
    private static final AudioFormat FORMAT = new AudioFormat(OpusPackets.SAMPLE_RATE, 16, 1, true, false);

    private final short[] samples;
    private final int loopStart;
    private volatile int offset;

    /**
     * @param offset    where to start, in samples, so a track can join an emote that is already running
     * @param loopStart where a repeat starts, or {@link OpusSound#NO_LOOP} to stop at the end
     */
    public PcmAudioStream(short[] samples, int offset, int loopStart) {
        this.samples = samples;
        this.loopStart = loopStart >= 0 && loopStart < samples.length ? loopStart : OpusSound.NO_LOOP;
        this.offset = wrap(offset, samples.length, this.loopStart);
    }

    private static int wrap(int offset, int length, int loopStart) {
        if (offset < 0) return 0;
        if (offset < length) return offset;
        if (loopStart < 0 || loopStart >= length) return length;
        return loopStart + (offset - loopStart) % (length - loopStart);
    }

    @Override
    public @NotNull AudioFormat getFormat() {
        return FORMAT;
    }

    @Override
    public @Nullable ByteBuffer read(int expectedSize) {
        int count = expectedSize / Short.BYTES;
        if (this.loopStart < 0) count = Math.min(count, this.samples.length - this.offset);
        if (count <= 0) return null;

        ByteBuffer buffer = BufferUtils.createByteBuffer(count * Short.BYTES);
        ShortBuffer pcm = buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        while (pcm.hasRemaining()) {
            if (this.offset == this.samples.length) this.offset = this.loopStart;
            int copied = Math.min(pcm.remaining(), this.samples.length - this.offset);
            pcm.put(this.samples, this.offset, copied);
            this.offset += copied;
        }
        return buffer;
    }

    /**
     * @return whether a track that does not loop has nothing left to hand over
     */
    public boolean exhausted() {
        return this.loopStart < 0 && this.offset == this.samples.length;
    }

    @Override
    public void close() {
        // Nothing to release, the samples outlive the stream
    }
}
