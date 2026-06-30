package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EmoteIconPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.ICON_PACKET;
    }

    @Override
    public byte getVer() {
        return 0x12;
    }

    @Override
    public void read(ByteBuf byteBuf, NetData config, byte version) throws IOException {
        if (byteBuf.readableBytes() < Integer.BYTES) {
            throw new IOException("Invalid icon packet: missing size");
        }

        int size = byteBuf.readInt();
        if (size <= 0) return;

        if (size > byteBuf.readableBytes() || size > CommonData.MAX_PACKET_SIZE) {
            throw new IOException("Invalid icon packet size: " + size);
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        byteBuf.readBytes(buffer);
        buffer.flip();
        config.extraData.put("iconData", buffer.asReadOnlyBuffer());
    }

    @Override
    public void write(ByteBuf byteBuf, NetData config, byte version) {
        assert config.emoteData != null;

        ByteBuffer iconData = config.emoteData.data().getBinary("iconData");
        if (iconData == null || !iconData.hasRemaining()) {
            byteBuf.writeInt(0);
            return;
        }

        byteBuf.writeInt(iconData.remaining());
        byteBuf.writeBytes(iconData.duplicate());
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
