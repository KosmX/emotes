package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.nio.ByteBuffer;

public class StopPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.STOP_PACKET;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public void read(ByteBuffer buf, NetData config, int version) {
        config.stopEmoteID = CommonNetwork.readUUID(buf);
    }

    @Override
    public void write(ByteBuffer buf, NetData config) {
        assert config.stopEmoteID != null;
        CommonNetwork.writeUUID(buf, config.stopEmoteID);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.stopEmoteID != null; //Write only if config has true stop value
    }

    @Override
    public int calculateSize(NetData config) {
        return Long.BYTES * 2; // 16
    }
}
