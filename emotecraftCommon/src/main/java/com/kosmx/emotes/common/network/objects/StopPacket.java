package com.kosmx.emotes.common.network.objects;

import com.kosmx.emotes.common.network.CommonNetwork;

import java.nio.ByteBuffer;

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
        return 0;
    }
}
