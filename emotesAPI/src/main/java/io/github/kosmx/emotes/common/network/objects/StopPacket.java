package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;

import java.nio.ByteBuffer;

public class StopPacket extends AbstractNetworkPacket {
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
        if (version < 1) return false;
        config.stopEmoteID = CommonNetwork.readUUID(buf);
        return true;
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        CommonNetwork.writeUUID(buf, config.stopEmoteID);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.stopEmoteID != null; //Write only if config has true stop value
    }

    @Override
    public int calculateSize(NetData config) {
        return Long.BYTES * 2; // 16
    }
}
