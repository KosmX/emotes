package io.github.kosmx.emotes.common.network.objects;

import java.nio.ByteBuffer;
import java.util.UUID;

public class StopPacket extends AbstractNetworkPacket {

    public StopPacket(){
    }

    @Override
    public byte getID() {
        return 10;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public boolean read(ByteBuffer buf, NetData config, int version){
        if(version < 1){
            return false;
        }
        long msb = buf.getLong();
        long lsb = buf.getLong();
        config.stopEmoteID = new UUID(msb, lsb);
        return true;
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        buf.putLong(config.stopEmoteID.getMostSignificantBits());
        buf.putLong(config.stopEmoteID.getLeastSignificantBits());
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.stopEmoteID != null; //Write only if config has true stop value
    }

    @Override
    public int calculateSize(NetData config) {
        return Long.BYTES*2; //16
    }
}
