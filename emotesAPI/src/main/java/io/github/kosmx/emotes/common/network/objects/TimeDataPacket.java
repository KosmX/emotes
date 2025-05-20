package io.github.kosmx.emotes.common.network.objects;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TimeDataPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return 55;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.startTime = byteBuffer.getLong();
        config.offsetTime = byteBuffer.get() != 0x00;
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        byteBuffer.putLong(config.startTime);
        byteBuffer.put(config.offsetTime ? (byte) 0x01 : (byte) 0x00);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.startTime > 0;
    }

    @Override
    public int calculateSize(NetData config) {
        return 9;
    }
}
