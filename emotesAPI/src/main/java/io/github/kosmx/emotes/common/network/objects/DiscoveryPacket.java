package io.github.kosmx.emotes.common.network.objects;


import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketTask;

import java.nio.ByteBuffer;
import java.util.HashMap;


public class DiscoveryPacket extends AbstractNetworkPacket{

    public DiscoveryPacket(){}


    @Override
    public boolean read(ByteBuffer buf, NetData data, int version){

        //Read these into versions
        int size = buf.getInt();
        HashMap<Byte, Byte> map = new HashMap<>();

        for(int i = 0; i < size; i++){
            byte id = buf.get();
            byte ver = buf.get();
            map.put(id, ver);
        }

        //check if every is exists, if not, return false
        //That is done somewhere else
        //apply changes
        data.versions = map;
        data.versionsUpdated = true;
        return true;
    }

    public void write(ByteBuffer buf, NetData data){
        //buf.putInt(this.version);
        buf.putInt(data.versions.size());
        data.versions.forEach((aByte, integer) -> {
            buf.put(aByte);
            buf.put(integer);
        });
    }

    @Override
    public byte getID() {
        return 8;
    }

    @Override
    public byte getVer() {
        return CommonData.networkingVersion;
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.CONFIG;
    }

    @Override
    public int calculateSize(NetData config) {
        //every keypair contains 2 bytes + the length
        return config.versions.size()*2 + 4;
    }
}
