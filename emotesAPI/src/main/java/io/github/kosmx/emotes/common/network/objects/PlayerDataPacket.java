package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlayerDataPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return PacketConfig.PLAYER_DATA_PACKET;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public void read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.player = CommonNetwork.readUUID(byteBuffer);
        if (version >= 1) config.isForced = byteBuffer.get() != 0x00;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        assert config.player != null;
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
