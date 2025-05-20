package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TimeDataPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.TIME_DATA_PACKET;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public void read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.otherTime = CommonNetwork.readBoolean(byteBuffer);
        config.startTime = byteBuffer.getLong();
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        CommonNetwork.writeBoolean(byteBuffer, config.otherTime);
        byteBuffer.putLong(config.startTime);
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
