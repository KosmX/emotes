package io.github.kosmx.emotes.common.network.objects;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class StopPacket extends AbstractNetworkPacket {

    public StopPacket(){
    }

    @Override
    public byte getID() {
        return 10;
    }

    @Override
    public byte getVer() {
        return 0;
    }

    @Override
    public boolean read(ByteBuffer buf, NetData config, int version){
        config.stopEmoteID = new AtomicInteger(buf.getInt());
        return true;
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        buf.putInt(config.stopEmoteID.get());
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.stopEmoteID != null; //Write only if config has true stop value
    }

    @Override
    public int calculateSize(NetData config) {
        return 4;
    }
}
