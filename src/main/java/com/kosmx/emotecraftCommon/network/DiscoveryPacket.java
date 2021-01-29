package com.kosmx.emotecraftCommon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

public class DiscoveryPacket {

    private int version;

    public DiscoveryPacket(int version){
        this.version = version;
    }
    public DiscoveryPacket(){}

    public int getVersion() {
        return version;
    }

    public void read(ByteBuf buf){
        this.version = buf.readInt();
    }

    public void write(ByteBuf buf){
        buf.writeInt(this.version);
    }
}
