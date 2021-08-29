package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.opennbs.network.NBSPacket;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SongPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 3;
    }

    @Override
    public byte getVer() {
        return 1; //Ver0 means NO sound
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        NBSPacket reader = new NBSPacket();
        reader.read(byteBuffer);
        config.song = reader.getSong();
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        if(!doWrite(config)){
            throw new IOException("You can't write disabled or not existing NBS data");
        }
        NBSPacket writer = new NBSPacket(config.emoteData.song);
        writer.write(byteBuffer);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.sendSong && config.versions.get(this.getID()) != 0 && config.emoteData != null && config.emoteData.song != null;
    }

    @Override
    public int calculateSize(NetData config) {
        if(config.emoteData == null || config.emoteData.song == null)return 0;
        return NBSPacket.calculateMessageSize(config.emoteData.song);
    }
}
