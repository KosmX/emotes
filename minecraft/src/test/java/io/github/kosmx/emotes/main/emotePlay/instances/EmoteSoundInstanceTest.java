package io.github.kosmx.emotes.main.emotePlay.instances;

import io.github.kosmx.emotes.common.opus.OpusSound;
import net.minecraft.client.sounds.AudioStream;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class EmoteSoundInstanceTest {
    @Test
    void decodeFailureStopsTheInstanceAndStillLetsItsChannelStop() throws Exception {
        OpusSound opus = new OpusSound(0, 0, 0, null, null,
                new byte[]{(byte) 0xfb, 0x41}, new int[]{0, 2});
        EmoteSoundInstance sound = sound(opus, 0);

        AudioStream stream = sound.stream().get(5, SECONDS);
        assertTrue(sound.isStopped());
        assertTrue(opus.failed());
        assertEquals(Short.BYTES, buffered(stream));
        assertNull(stream.read(96000));
    }

    @Test
    void stoppedInstanceDoesNotStartCachedAudio() throws Exception {
        OpusSound opus = silence();
        opus.decoded().get(5, SECONDS);
        EmoteSoundInstance sound = sound(opus, 0);
        sound.stop();

        AudioStream stream = sound.stream().get(5, SECONDS);
        assertEquals(Short.BYTES, buffered(stream));
        assertNull(stream.read(96000));
        assertFalse(opus.failed());
    }

    @Test
    void emoteThatOutranTheTrackWhileItDecodedHandsOverSilence() throws Exception {
        OpusSound opus = silence();
        EmoteSoundInstance sound = sound(opus, opus.playable());

        AudioStream stream = sound.stream().get(5, SECONDS);
        assertEquals(Short.BYTES, buffered(stream));
        assertNull(stream.read(96000));
    }

    @Test
    void joinsTheEmoteAtTheOffsetTakenBeforeTheDecode() throws Exception {
        OpusSound opus = silence();
        EmoteSoundInstance sound = sound(opus, 480);
        // The engine takes the volume before it asks for the stream
        assertEquals(0.0F, sound.getVolume());

        AudioStream stream = sound.stream().get(5, SECONDS);
        assertEquals((opus.playable() - 480) * Short.BYTES, buffered(stream));
        assertNull(stream.read(96000));
        assertTrue(sound.canStartSilent());
    }

    @Test
    void aTrackRunsOutOnlyWhileTheEngineIsPlayingIt() throws Exception {
        OpusSound opus = silence(null);
        EmoteSoundInstance sound = sound(opus, 0);

        sound.stream().get(5, SECONDS);
        // A paused game keeps the channel and the ticks, so wall time on its own must not run the track out
        Thread.sleep(opus.durationMs() + 10L);
        assertFalse(sound.finished());
    }

    @Test
    void aLoopingTrackNeverRunsOut() throws Exception {
        EmoteSoundInstance sound = sound(silence(0), 0);

        sound.stream().get(5, SECONDS);
        assertFalse(sound.finished());
    }

    private static int buffered(AudioStream stream) throws Exception {
        var buffer = stream.read(96000);
        assertNotNull(buffer);
        return buffer.remaining();
    }

    private static OpusSound silence() throws Exception {
        return silence(null);
    }

    private static OpusSound silence(Integer loopStart) throws Exception {
        byte[] silence = {(byte) 0xf8, (byte) 0xff, (byte) 0xfe};
        return new OpusSound(0, 0, 0, null, loopStart, silence, new int[]{0, silence.length});
    }

    private static EmoteSoundInstance sound(OpusSound opus, int offset) {
        return new EmoteSoundInstance(null, opus, () -> offset) {};
    }
}
