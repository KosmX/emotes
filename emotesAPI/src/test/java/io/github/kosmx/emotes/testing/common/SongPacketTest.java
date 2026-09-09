package io.github.kosmx.emotes.testing.common;

import io.github.jaredmdobson.concentus.OpusApplication;
import io.github.jaredmdobson.concentus.OpusEncoder;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.common.opus.OpusFormatException;
import io.github.kosmx.emotes.common.opus.Loudness;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.util.network.VarIntUtils;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SongPacketTest {
    private static final byte[] SILENCE = {(byte) 0xf8, (byte) 0xff, (byte) 0xfe};
    private static final byte[] NBS = {1, 2, 3, 4};

    @Test
    void preservesOutputGainAndPacketsInStreamsAndFiles() throws Exception {
        for (PacketTask purpose : new PacketTask[]{PacketTask.STREAM, PacketTask.FILE}) {
            for (Integer trackGain : new Integer[]{null, 0, -1536, 2048}) {
                for (int outputGain : new int[]{Short.MIN_VALUE, -5120, -1, 0, 1, 2560, Short.MAX_VALUE}) {
                    NetData data = roundTrip(purpose, new SongPacket().getVer(), outputGain, trackGain);
                    OpusSound sound = (OpusSound) data.emoteData.data().getRaw(SongPacket.OPUS_KEY);
                    assertEquals(outputGain, sound.outputGain());
                    assertEquals(120, sound.preSkip());
                    assertEquals(60, sound.endTrim());
                    assertEquals(240, sound.loopStart());
                    assertEquals(1, sound.packetCount());
                    assertArrayEquals(SILENCE, sound.data());
                    assertEquals(purpose == PacketTask.FILE ? ByteBuffer.wrap(NBS) : null,
                            data.emoteData.data().getBinary(SongPacket.NBS_KEY));
                }
            }
        }
    }

    @Test
    void keepsVersionThreeAndZeroGainWireFormat() throws Exception {
        NetData data = roundTrip(PacketTask.FILE, (byte) 3, 0, null);
        OpusSound sound = (OpusSound) data.emoteData.data().getRaw(SongPacket.OPUS_KEY);
        assertEquals(0, sound.outputGain());
        assertEquals(ByteBuffer.wrap(NBS), data.emoteData.data().getBinary(SongPacket.NBS_KEY));

        SongPacket packet = new SongPacket();
        assertEquals(3, packet.getVer());
        data.versions.put(packet.getID(), (byte) 3);
        assertEquals(3, packet.getVer(data));
        data.versions.put(packet.getID(), (byte) 4);
        assertEquals(3, packet.getVer(data));
    }

    @Test
    void preservesMaximumUnsignedPreSkipWithSignedGain() throws Exception {
        byte[] data = new byte[70 * SILENCE.length];
        int[] offsets = new int[71];
        for (int i = 0; i < 70; i++) {
            offsets[i] = i * SILENCE.length;
            System.arraycopy(SILENCE, 0, data, offsets[i], SILENCE.length);
        }
        offsets[70] = data.length;
        for (int gain : new int[]{Short.MIN_VALUE, Short.MAX_VALUE}) {
            OpusSound original = new OpusSound(65535, 0, gain, null, null, data, offsets);
            NetData received = roundTrip(PacketTask.STREAM, (byte) 3, original);
            OpusSound sound = (OpusSound) received.emoteData.data().getRaw(SongPacket.OPUS_KEY);
            assertEquals(65535, sound.preSkip());
            assertEquals(gain, sound.outputGain());
        }
    }

    @Test
    void rejectsValuesThatWouldOverlapPackedFields() {
        assertThrows(OpusFormatException.class,
                () -> new OpusSound(65536, 0, 0, null, null, SILENCE, new int[]{0, 3}));
        for (int gain : new int[]{Short.MIN_VALUE - 1, Short.MAX_VALUE + 1}) {
            assertThrows(OpusFormatException.class,
                    () -> new OpusSound(0, 0, gain, null, null, SILENCE, new int[]{0, 3}));
        }
    }

    @Test
    void localTrackGainAndRemoteMeasurementNormalizeTheSameAudio() throws Exception {
        // Generate a non-silent fixture in the test only; production never re-encodes packets.
        OpusEncoder encoder = new OpusEncoder(48000, 1, OpusApplication.OPUS_APPLICATION_AUDIO);
        encoder.setBitrate(32000);
        ByteArrayOutputStream packets = new ByteArrayOutputStream();
        int[] offsets = new int[51];
        short[] frame = new short[960];
        byte[] encoded = new byte[1275];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < frame.length; j++) {
                frame[j] = (short) (28000 * Math.sin(2 * Math.PI * 440 * (i * frame.length + j) / 48000));
            }
            offsets[i] = packets.size();
            int length = encoder.encode(frame, 0, frame.length, encoded, 0, encoded.length);
            packets.write(encoded, 0, length);
        }
        offsets[50] = packets.size();
        byte[] data = packets.toByteArray();
        OpusSound measured = new OpusSound(120, 60, -256, null, null, data, offsets);
        OpusSound.DecodedSound reference = measured.decoded().join();
        Loudness loudness = new Loudness(reference.samples().length);
        loudness.feed(reference.samples(), 0, reference.samples().length);
        int trackGain = (int) Math.round((-23.0 - loudness.integrated()) * 256.0);
        OpusSound local = new OpusSound(120, 60, -256, trackGain, null, data, offsets);

        for (PacketTask purpose : new PacketTask[]{PacketTask.STREAM, PacketTask.FILE}) {
            NetData received = roundTrip(purpose, (byte) 3, local);
            OpusSound remote = (OpusSound) received.emoteData.data().getRaw(SongPacket.OPUS_KEY);
            OpusSound.DecodedSound decoded = remote.decoded().join();
            assertArrayEquals(data, remote.data());
            assertArrayEquals(local.decoded().join().samples(), decoded.samples());
            assertTrue(decoded.normalization() > 0 && decoded.normalization() < 1);
            // R128 tags round to 1/256 dB; the PCM measurement is not quantized.
            assertEquals(local.decoded().join().normalization(), decoded.normalization(), 0.001F);
        }
    }

    @Test
    void stillUsesLocalTrackGainWhenPresent() throws Exception {
        OpusSound local = new OpusSound(120, 60, 0, -8192, null, SILENCE, new int[]{0, 3});
        assertTrue(local.decoded().join().normalization() < 1);
        NetData received = roundTrip(PacketTask.STREAM, (byte) 3, local);
        OpusSound remote = (OpusSound) received.emoteData.data().getRaw(SongPacket.OPUS_KEY);
        // Deliberately inconsistent tag: only local playback should use it; measured silence needs no attenuation.
        assertEquals(1.0F, remote.decoded().join().normalization());
    }

    @Test
    void readingLivePlaybackDoesNotStartDecoding() throws Exception {
        NetData data = roundTrip(PacketTask.STREAM, new SongPacket().getVer(), 0, null);
        OpusSound sound = (OpusSound) data.emoteData.data().getRaw(SongPacket.OPUS_KEY);
        // Cover both an in-flight decode and one that already finished on the worker thread.
        var decoding = OpusSound.class.getDeclaredField("decoding");
        var pcm = OpusSound.class.getDeclaredField("pcm");
        var failed = OpusSound.class.getDeclaredField("failed");
        decoding.setAccessible(true);
        pcm.setAccessible(true);
        failed.setAccessible(true);
        assertNull(decoding.get(sound));
        assertNull(((SoftReference<?>) pcm.get(sound)).get());
        assertFalse(failed.getBoolean(sound));
    }

    private NetData roundTrip(PacketTask purpose, byte version, int outputGain, Integer trackGain) throws Exception {
        return roundTrip(purpose, version,
                new OpusSound(120, 60, outputGain, trackGain, 240, SILENCE.clone(), new int[]{0, SILENCE.length}));
    }

    private NetData roundTrip(PacketTask purpose, byte version, OpusSound sound) throws Exception {
        SongPacket packet = new SongPacket();
        NetData source = new NetData();
        source.purpose = purpose;
        source.emoteData = RandomEmoteData.generateEmotes().left();
        source.emoteData.data().put(SongPacket.OPUS_KEY, sound);
        source.emoteData.data().put(SongPacket.NBS_KEY, ByteBuffer.wrap(NBS));

        NetData target = new NetData();
        target.purpose = purpose;
        target.playback = true;
        target.emoteData = RandomEmoteData.generateEmotes().left();
        ByteBuf buf = Unpooled.buffer();
        try {
            packet.write(buf, source, version);
            // The existing four header VarInts are followed immediately by packet lengths and bytes.
            ByteBuf wire = buf.duplicate();
            int skipAndGain = VarIntUtils.readVarInt(wire);
            assertEquals(sound.preSkip(), skipAndGain & 0xffff);
            assertEquals(sound.outputGain(), skipAndGain >> 16);
            assertEquals(sound.endTrim(), VarIntUtils.readVarInt(wire));
            assertEquals(sound.loopStart() + 1, VarIntUtils.readVarInt(wire));
            assertEquals(sound.packetCount(), VarIntUtils.readVarInt(wire));
            for (int i = 0; i < sound.packetCount(); i++) {
                assertEquals(sound.length(i), VarIntUtils.readVarInt(wire));
                for (int j = 0; j < sound.length(i); j++) assertEquals(sound.data()[sound.offset(i) + j], wire.readByte());
            }
            assertEquals(purpose == PacketTask.FILE ? NBS.length : 0, wire.readableBytes());
            if (sound.outputGain() == 0 && sound.preSkip() == 120 && sound.loopStart() == 240) {
                byte[] original = {120, 60, (byte) 0xf1, 1, 1, 3, (byte) 0xf8, (byte) 0xff, (byte) 0xfe};
                byte[] actual = new byte[original.length];
                buf.getBytes(buf.readerIndex(), actual);
                assertArrayEquals(original, actual);
            }
            packet.read(buf, target, version);
            assertFalse(buf.isReadable());
            assertTrue(target.prepareAndValidate());
            return target;
        } finally {
            buf.release();
        }
    }
}
