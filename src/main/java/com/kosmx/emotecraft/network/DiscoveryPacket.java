package com.kosmx.emotecraft.network;

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

    public void read(PacketByteBuf buf){
        this.version = buf.readInt();
    }

    public void write(PacketByteBuf buf){
        buf.writeInt(this.version);
    }
}
