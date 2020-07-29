package com.kosmx.emotecraft.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class StopPacket {
    protected UUID player;

    public StopPacket(){}

    public StopPacket(PlayerEntity playerEntity){
        this.player = playerEntity.getGameProfile().getId();
    }
    public void read(PacketByteBuf buf){
        player = buf.readUuid();
    }
    public UUID getPlayer(){
        return this.player;
    }
    public void write(PacketByteBuf buf){
        buf.writeUuid(player);
    }
}
