package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.AnimationBinary;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NewAnimPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.NEW_ANIMATION_FORMAT;
    }

    @Override
    public byte getVer() {
        return AnimationBinary.CURRENT_VERSION;
    }

    @Override
    public void read(ByteBuffer buf, NetData config, int version) throws IOException {
        config.tick = buf.getFloat();
        config.emoteData = AnimationBinary.read(Unpooled.wrappedBuffer(buf), version);
        config.valid = true; // TODO
    }

    @Override
    public void write(ByteBuffer buf, NetData config) throws IOException {
        assert config.emoteData != null;
        buf.putFloat(config.tick);

        ByteBuf tempNettyBuf = Unpooled.buffer();
        try {
            AnimationBinary.write(tempNettyBuf, getVer(config.versions), config.emoteData);
            buf.put(tempNettyBuf.array(), 0, tempNettyBuf.readableBytes());
        } finally {
            tempNettyBuf.release();
        }
    }

    @Override
    public boolean doWrite(NetData data) {
        return data.emoteData != null && data.stopEmoteID == null && data.versions.containsKey(PacketConfig.NEW_ANIMATION_FORMAT);
    }

    @Override
    public int calculateSize(NetData config) {
        if (config.emoteData == null || !config.versions.containsKey(PacketConfig.NEW_ANIMATION_FORMAT)) return 0;
        ByteBuf byteBuf = Unpooled.buffer();
        try {
            AnimationBinary.write(byteBuf, getVer(config.versions), config.emoteData);
            return byteBuf.readableBytes() + 4;
        } finally {
            byteBuf.release();
        }
    }
}
