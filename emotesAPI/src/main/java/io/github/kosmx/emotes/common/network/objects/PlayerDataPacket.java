package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlayerDataPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 1;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.player = CommonNetwork.readUUID(byteBuffer);
        if (version >= 1) config.isForced = byteBuffer.get() != 0x00;
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        CommonNetwork.writeUUID(byteBuffer, config.player);
        byteBuffer.put(config.isForced ? (byte) 0x01 : (byte) 0x00);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.player != null;
    }

    @Override
    public int calculateSize(NetData config) {
        return 17;//1 UUID = 2 Long = 2*8 bytes = 16 bytes + 1 byte for forced flag
    }
}
