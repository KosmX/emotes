package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * It should be placed into emotecraftCommon, but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {

    public int tick = 0;

    public EmoteDataPacket(){
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        int version = calculateVersion(config);
        assert config.emoteData != null;
        buf.putInt(config.tick);
        AnimationBinary.write(config.emoteData, buf, version);
    }

    @Override
    public boolean read(ByteBuffer buf, NetData config, int version) throws IOException {
        try {
            config.tick = buf.getInt();
            KeyframeAnimation animation = AnimationBinary.read(buf, version);

            config.valid = (boolean) animation.extraData.get("valid");

            config.emoteBuilder = animation.mutableCopy();

            config.wasEmoteData = true;

            return true;
        } catch(IOException|RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public byte getID() {
        return 0;
    }

    /**
     * version 1: 2.1 features, extended parts, UUID emote ID
     * version 2: Animation library, dynamic parts
     */
    @Override
    public byte getVer() {
        return 2;
    }

    protected int calculateVersion(NetData config) {
        return Math.min(config.versions.get(getID()), getVer());
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
        if(config.emoteData == null)return 0;
        return AnimationBinary.calculateSize(config.emoteData, calculateVersion(config)) + 4;
    }

}
