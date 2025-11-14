package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.AnimationBinary;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;

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
    public void read(ByteBuf buf, NetData config, byte version) {
        config.tick = buf.readFloat();
        config.emoteData = AnimationBinary.read(buf, version);
        config.valid = true; // TODO
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) {
        assert config.emoteData != null;

        buf.writeFloat(config.tick);
        AnimationBinary.write(buf, version, config.emoteData);
    }

    @Override
    public boolean doWrite(NetData data) {
        return data.emoteData != null && data.stopEmoteID == null && data.versions.containsKey(PacketConfig.NEW_ANIMATION_FORMAT);
    }
}
