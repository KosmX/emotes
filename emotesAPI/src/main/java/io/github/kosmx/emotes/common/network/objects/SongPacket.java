package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.util.network.VarIntUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SongPacket extends AbstractNetworkPacket {
    public static final String NBS_KEY = "song";
    public static final String OPUS_KEY = "sound";

    // Ver0 means NO sound, ver1 was the legacy note list
    private static final byte NBS_VERSION = 2;
    private static final byte OPUS_VERSION = 3;

    @Override
    public byte getID() {
        return PacketConfig.NBS_CONFIG;
    }

    @Override
    public byte getVer() {
        return OPUS_VERSION;
    }

    /**
     * Opus if the other side speaks it, otherwise the .nbs file if there is one to pass through.
     */
    @Override
    public byte getVer(NetData config) {
        if (config.emoteData == null) return 0;

        byte version = super.getVer(config);
        ExtraAnimationData data = config.emoteData.data();

        if (version >= OPUS_VERSION && data.getRaw(OPUS_KEY) instanceof OpusSound) return OPUS_VERSION;
        // getBinary writes back into the emote, and this runs once per recipient
        if (version < NBS_VERSION || !data.has(NBS_KEY)) return 0;

        // Nothing plays .nbs any more: it goes to someone who cannot read Opus, or into a stored emote
        return version == NBS_VERSION || config.purpose == PacketTask.FILE ? NBS_VERSION : 0;
    }

    @Override
    public void read(ByteBuf buf, NetData config, byte version) throws IOException {
        switch (version) {
            case OPUS_VERSION -> {
                // Both OpusHead values fit in the existing VarInt: unsigned skip, signed Q7.8 gain
                int skipAndGain = VarIntUtils.readVarInt(buf);
                int preSkip = skipAndGain & 0xffff;
                int outputGain = skipAndGain >> 16;
                int endTrim = VarIntUtils.readVarInt(buf);
                int loopStart = VarIntUtils.readVarInt(buf) - 1;

                int count = VarIntUtils.readVarInt(buf);
                // Every packet costs a length byte and a TOC byte, so the count can be sized against what is left
                if (count < 0 || count > buf.readableBytes() / 2) throw new IOException("Invalid Opus packet count: " + count);

                int[] offsets = new int[count + 1];
                byte[] data = new byte[buf.readableBytes()]; // the rest of the sub-packet bounds the packets
                int length = 0;

                for (int i = 0; i < count; i++) {
                    int size = VarIntUtils.readVarInt(buf);
                    if (size <= 0 || size > buf.readableBytes()) throw new IOException("Invalid Opus packet size: " + size);

                    offsets[i] = length;
                    buf.readBytes(data, length, size);
                    length += size;
                }
                offsets[count] = length;

                // Without a local R128 tag, the decoder measures loudness from the PCM
                OpusSound sound = new OpusSound(preSkip, endTrim, outputGain, null, loopStart < 0 ? null : loopStart,
                        Arrays.copyOf(data, length), offsets);
                config.extraData.put(OPUS_KEY, sound);

                // Only a stored emote carries the .nbs tail; anywhere else trailing bytes are junk
                if (config.purpose == PacketTask.FILE && buf.isReadable()) config.extraData.put(NBS_KEY, song(buf));
            }

            // Nothing plays .nbs any more: only a live emote, on the side that plays it, has no use left for it
            case NBS_VERSION -> {
                if (config.purpose != PacketTask.STREAM || !config.playback) config.extraData.put(NBS_KEY, song(buf));
            }

            // Ver1 held a note list rather than a file, so there is nothing left that can read or relay it
            default -> CommonData.LOGGER.warn("Dropping the sound of an emote: sub-packet version {} is no longer supported", version);
        }
    }

    /** Stored the way {@link ExtraAnimationData#getBinary} wants it, so that reading it never writes. */
    private static ByteBuffer song(ByteBuf buf) {
        return ByteBuffer.wrap(MathHelper.readBytes(buf)).asReadOnlyBuffer();
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) throws IOException {
        assert config.emoteData != null;
        ExtraAnimationData data = config.emoteData.data();

        if (version == OPUS_VERSION) {
            if (!(data.getRaw(OPUS_KEY) instanceof OpusSound sound)) throw new IOException("Emote has no Opus sound");

            VarIntUtils.writeVarInt(buf, sound.preSkip() | (sound.outputGain() << 16));
            VarIntUtils.writeVarInt(buf, sound.endTrim());
            VarIntUtils.writeVarInt(buf, sound.loopStart() + 1);
            VarIntUtils.writeVarInt(buf, sound.packetCount());

            for (int i = 0, count = sound.packetCount(); i < count; i++) {
                VarIntUtils.writeVarInt(buf, sound.length(i));
                buf.writeBytes(sound.data(), sound.offset(i), sound.length(i));
            }

            ByteBuffer song = config.purpose == PacketTask.FILE ? data.getBinary(NBS_KEY) : null;
            if (song != null) buf.writeBytes(song);
        } else {
            ByteBuffer song = data.getBinary(NBS_KEY);
            if (song == null) throw new IOException("Emote has no song");
            buf.writeBytes(song);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return getVer(config) != 0;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

}
