package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.opus.OpusSound;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class PcmAudioStreamTest {
    @Test
    void fillsBuffersAcrossShortLoopBoundaries() {
        PcmAudioStream stream = new PcmAudioStream(new short[]{1, 2, 3}, 0, 1);
        assertArrayEquals(new short[]{1, 2, 3, 2, 3, 2, 3, 2}, samples(stream.read(16)));
        assertArrayEquals(new short[]{3, 2, 3, 2}, samples(stream.read(8)));
        assertFalse(stream.exhausted());
    }

    @Test
    void fillsOneSecondBufferWithTwoAndAHalfMillisecondLoop() {
        short[] loop = new short[120];
        for (short i = 0; i < loop.length; i++) loop[i] = i;
        PcmAudioStream stream = new PcmAudioStream(loop, 0, 0);
        short[] actual = samples(stream.read(96000));
        assertEquals(48000, actual.length);
        for (int i = 0; i < actual.length; i++) assertEquals(loop[i % loop.length], actual[i]);
        assertFalse(stream.exhausted());
    }

    @Test
    void nonLoopingTrackReturnsItsTailThenEof() {
        PcmAudioStream stream = new PcmAudioStream(new short[]{1, 2, 3}, 1, OpusSound.NO_LOOP);
        assertFalse(stream.exhausted());
        assertArrayEquals(new short[]{2, 3}, samples(stream.read(16)));
        assertTrue(stream.exhausted());
        assertNull(stream.read(16));
    }

    @Test
    void wrapsLateStartAndKeepsWholeSamples() {
        PcmAudioStream stream = new PcmAudioStream(new short[]{1, 2, 3}, 6, 1);
        assertNull(stream.read(1));
        assertArrayEquals(new short[]{3, 2}, samples(stream.read(5)));
    }

    @Test
    void invalidAndEmptyLoopsEndWithoutSpinning() {
        assertNull(new PcmAudioStream(new short[0], 0, 0).read(16));
        PcmAudioStream stream = new PcmAudioStream(new short[]{1}, 0, 1);
        assertArrayEquals(new short[]{1}, samples(stream.read(16)));
        assertNull(stream.read(16));
    }

    private short[] samples(ByteBuffer buffer) {
        assertNotNull(buffer);
        assertEquals(0, buffer.position());
        short[] result = new short[buffer.remaining() / Short.BYTES];
        buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(result);
        return result;
    }
}
