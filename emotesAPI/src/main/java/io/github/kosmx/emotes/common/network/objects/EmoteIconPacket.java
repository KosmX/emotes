package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class EmoteIconPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return PacketConfig.ICON_PACKET;
    }

    @Override
    public byte getVer() {
        return 0x12;
    }

    @Override
    public void read(ByteBuf byteBuf, NetData config, byte version) {
        int size = byteBuf.readInt();
        if (size != 0) {
            byte[] bytes = new byte[size];
            byteBuf.readBytes(bytes);
            config.extraData.put("iconData", ByteBuffer.wrap(bytes));
        }
    }

    @Override
    public void write(ByteBuf byteBuf, NetData config, byte version) {
        assert config.emoteData != null;
        ByteBuffer iconData = (ByteBuffer)config.emoteData.data().getRaw("iconData");

        try {
            byteBuf.writeInt(iconData.remaining());
            byteBuf.writeBytes(iconData);
        } finally {
            iconData.position(0);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.FILE && config.emoteData != null && config.emoteData.data().has("iconData");
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
