package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EmoteIconPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0;
    }

    @Override
    public byte getVer() {
        return 0;
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
        return config.purpose == PacketTask.FILE;
    }

    @Override
    public int calculateSize(NetData config) {
        return 0;
    }
}
