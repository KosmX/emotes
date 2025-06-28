package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.AnimationBinary;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * It should be placed into emotecraftCommon, but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {
    @Override
    public void write(ByteBuffer buf, NetData config) {
        int version = getVer(config.versions);
        assert config.emoteData != null;
        buf.putInt((int) config.tick);
        LegacyAnimationBinary.write(config.emoteData, buf, version);
    }

    @Override
    public void read(ByteBuffer buf, NetData config, int version) throws IOException {
        config.tick = buf.getInt();
        config.emoteData = LegacyAnimationBinary.read(buf, version);
        config.valid = config.emoteData.data().<Boolean>get("valid").orElse(false);
    }

    @Override
    public byte getID() {
        return PacketConfig.ANIMATION_FORMAT;
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
        return data.emoteData != null && data.stopEmoteID == null;
    }

    /*
    Data types in comment:
    I int, 4 bytes
    L Long 8 bytes (1 uuid = 2 L)
    B byte, ...1 byte
    F float, 4 bytes
     */
    @Override
    public int calculateSize(NetData config) {
        if (config.emoteData == null) return 0;
        return LegacyAnimationBinary.calculateSize(config.emoteData, getVer(config.versions)) + 4;
    }
}
