package io.github.kosmx.emotes.common.network.objects;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractNetworkPacket {
    public abstract byte getID();
    public abstract byte getVer();

    public byte getVer(Map<Byte, Byte> versions) {
        if (!versions.containsKey(this.getID())) throw new IllegalArgumentException("Versions should contain it's id");
        return (byte) Math.min(this.getVer(), versions.get(this.getID()));
    }

    public abstract void read(ByteBuf byteBuf, NetData config, byte version) throws IOException;
    public abstract void write(ByteBuf byteBuf, NetData config, byte version) throws IOException;

    public abstract boolean doWrite(NetData config);

    public boolean isOptional() {
        return false;
    }
}
