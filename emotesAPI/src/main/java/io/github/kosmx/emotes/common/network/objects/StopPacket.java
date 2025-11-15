package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.NetworkUtils;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;

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
    public void read(ByteBuf buf, NetData config, byte version) {
        config.stopEmoteID = NetworkUtils.readUuid(buf);
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) {
        assert config.stopEmoteID != null;
        NetworkUtils.writeUuid(buf, config.stopEmoteID);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.stopEmoteID != null; // Write only if config has true stop value
    }
}
