package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.NetworkUtils;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;

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
    public void read(ByteBuf byteBuf, NetData config, byte version) {
        config.player = NetworkUtils.readUuid(byteBuf);
        if (version >= 1) config.isForced = byteBuf.readByte() != 0x00;
    }

    @Override
    public void write(ByteBuf byteBuf, NetData config, byte version) {
        assert config.player != null;

        NetworkUtils.writeUuid(byteBuf, config.player);
        if (version >= 1) byteBuf.writeByte(config.isForced ? (byte) 0x01 : (byte) 0x00);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.player != null;
    }
}
