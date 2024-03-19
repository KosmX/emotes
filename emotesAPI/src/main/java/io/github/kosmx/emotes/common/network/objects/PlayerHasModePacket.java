package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketTask;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PlayerHasModePacket extends AbstractNetworkPacket{//TODO HAS MODE
    @Override
    public byte getID() {
        return 2;
    }

    @Override
    public byte getVer() {
        return 1;
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.player = CommonNetwork.readUUID(byteBuffer);
        config.removeFromList = byteBuffer.get() != 0x00;
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        CommonNetwork.writeUUID(byteBuffer, config.player);
        byteBuffer.put(config.removeFromList ? (byte) 0x01 : (byte) 0x00);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.PLAYER_LIST && config.player != null;
    }

    @Override
    public int calculateSize(NetData config) {
        return 17;
    }
}
