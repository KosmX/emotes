package com.kosmx.emotes.common.network.objects;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SongPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 3;
    }

    @Override
    public byte getVer() {
        return 0; //Ver0 means NO sound //TODO enable it.
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        return false;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {

    }

    @Override
    public boolean doWrite(NetData config) {
        return config.versions.get(this.getID()) != 0 && config.emoteData != null && config.emoteData.song != null;
    }

    @Override
    public int calculateSize(NetData config) {
        return 0; //uh, that won't be easy to calculate...//TODO
    }
}
