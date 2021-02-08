package com.kosmx.emotecraftCommon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;


import java.util.UUID;

public class StopPacket {
    protected UUID player;

    public StopPacket(){
    }

    public StopPacket(PlayerEntity playerEntity){
        this.player = playerEntity.getGameProfile().getId();
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
