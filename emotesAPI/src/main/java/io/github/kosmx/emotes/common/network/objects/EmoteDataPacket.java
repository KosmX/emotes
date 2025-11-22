package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * It should be placed into emotecraftCommon, but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {
    @Override
    public void write(ByteBuf buf, NetData config, byte version) {
        assert config.emoteData != null;
        buf.writeInt((int) config.tick);
        LegacyAnimationBinary.write(config.emoteData, buf, version);
    }

    @Override
    public void read(ByteBuf buf, NetData config, byte version) throws IOException {
        config.tick = buf.readInt();
        config.emoteData = LegacyAnimationBinary.read(buf, version);
        config.valid = true; // TODO
    }

    @Override
    public byte getID() {
        return PacketConfig.LEGACY_ANIMATION_FORMAT;
    }

    /**
     * version 1: 2.1 features, extended parts, UUID emote ID
     * version 2: Animation library, dynamic parts
     * version 3: Animation scale
     * version 4: easing args
     */
    @Override
    public byte getVer() {
        return (byte) LegacyAnimationBinary.getCurrentVersion();
    }

    @Override
    public boolean doWrite(NetData data) {
        return data.emoteData != null && data.stopEmoteID == null && !data.versions.containsKey(PacketConfig.NEW_ANIMATION_FORMAT);
    }
}
