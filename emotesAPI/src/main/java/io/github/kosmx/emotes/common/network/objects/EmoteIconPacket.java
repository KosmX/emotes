package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketTask;

import java.io.IOException;
import java.nio.Buffer;
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
            config.extraData.put("iconData", ByteBuffer.wrap(bytes));
        }
        return true;
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        assert config.emoteData != null;
        ByteBuffer iconData = (ByteBuffer)config.emoteData.extraData.get("iconData");
        byteBuffer.putInt(iconData.remaining());
        byteBuffer.put(iconData);
        ((Buffer)iconData).position(0);
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.FILE && config.emoteData != null && config.emoteData.extraData.containsKey("iconData");
    }

    @Override
    public int calculateSize(NetData config) {
        if (config.emoteData == null) return 0;
        return ((ByteBuffer)config.emoteData.extraData.get("iconData")).remaining() + 4;
    }
}
