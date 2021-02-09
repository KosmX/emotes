package com.kosmx.emotecraftCommon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;


import java.util.UUID;

public class StopPacket {
    protected UUID player;

    public StopPacket(){
    }

    public StopPacket(UUID playerEntity){
        this.player = playerEntity;
    }

    public void read(ByteBuf buf){
        player = CommonNetwork.readUUID(buf);
    }

    public UUID getPlayer(){
        return this.player;
    }

    public void write(ByteBuf buf){
        CommonNetwork.writeUUID(buf, player);
    }
}
