package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EmoteIconPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0x12;
    }

    @Override
    public byte getVer() {
        return 0x12;
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        int size = byteBuffer.getInt();
        if(size != 0) {
            byte[] bytes = new byte[size];
            byteBuffer.get(bytes);
            config.iconData = ByteBuffer.wrap(bytes);
        }
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        byteBuffer.putInt(config.emoteData.iconData.remaining());
        ByteBuffer icon = config.emoteData.iconData;
        byteBuffer.put(icon);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.FILE && config.emoteData.iconData != null;
    }

    @Override
    public int calculateSize(NetData config) {
        return config.emoteData.iconData.remaining() + 4;
    }
}
