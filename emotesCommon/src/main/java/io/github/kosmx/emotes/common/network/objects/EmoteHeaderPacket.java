package io.github.kosmx.emotes.common.network.objects;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EmoteHeaderPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0x11;
    }

    @Override
    public byte getVer() {
        return 1;
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
        return config.purpose.exchangeHeader;
    }

    @Override
    public int calculateSize(NetData config) {
        return 0;
    }
}
