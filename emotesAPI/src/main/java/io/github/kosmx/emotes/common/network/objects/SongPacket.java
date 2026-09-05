package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.util.network.VarIntUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
        return version >= NBS_VERSION && data.getBinary(NBS_KEY) != null ? NBS_VERSION : 0;
    }

    @Override
    public void read(ByteBuf buf, NetData config, byte version) throws IOException {
        switch (version) {
            case OPUS_VERSION -> {
                int preSkip = buf.readUnsignedShort();
                int loopStart = VarIntUtils.readVarInt(buf) - 1;

                OpusSound sound = new OpusSound(preSkip, 0, null, loopStart < 0 ? null : loopStart, readPackets(buf));
                // Only the side that plays a live emote decodes; servers and proxies just relay the packets
                if (config.purpose == PacketTask.STREAM && config.bound == PacketBound.CLIENT) sound.pcm();
                config.extraData.put(OPUS_KEY, sound);

                // A stored emote keeps its .nbs too, so it can still be streamed to someone who needs it
                if (buf.isReadable()) config.extraData.put(NBS_KEY, MathHelper.readBytes(buf));
            }

            case NBS_VERSION -> config.extraData.put(NBS_KEY, MathHelper.readBytes(buf));
        }
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) throws IOException {
        assert config.emoteData != null;
        ExtraAnimationData data = config.emoteData.data();

        if (version == OPUS_VERSION) {
            if (!(data.getRaw(OPUS_KEY) instanceof OpusSound sound)) throw new IOException("Emote has no Opus sound");

            buf.writeShort(sound.preSkip());
            VarIntUtils.writeVarInt(buf, sound.loopStart() + 1);
            VarIntUtils.writeVarInt(buf, sound.packets().size());

            for (byte[] packet : sound.packets()) { // Avoid lambda
                VarIntUtils.writeVarInt(buf, packet.length);
                buf.writeBytes(packet);
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

    private static List<byte[]> readPackets(ByteBuf buf) throws IOException {
        int count = VarIntUtils.readVarInt(buf);
        // Every packet costs a length byte and a TOC byte, so the count can be sized against what is left
        if (count < 0 || count > buf.readableBytes() / 2) throw new IOException("Invalid Opus packet count: " + count);

        List<byte[]> packets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int length = VarIntUtils.readVarInt(buf);
            if (length <= 0 || length > buf.readableBytes()) throw new IOException("Invalid Opus packet size: " + length);

            byte[] packet = new byte[length];
            buf.readBytes(packet);
            packets.add(packet);
        }
        return packets;
    }
}
