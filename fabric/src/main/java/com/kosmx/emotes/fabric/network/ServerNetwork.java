package com.kosmx.emotes.fabric.network;

import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.common.network.objects.NetData;
import com.kosmx.emotes.executor.INetworkInstance;
import com.kosmx.emotes.server.AbstractServerEmotePlay;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.UUID;


public class ServerNetwork extends AbstractServerEmotePlay<PlayerEntity> {
    public static final Identifier channelID = new Identifier(CommonData.MOD_ID, CommonData.playEmoteID);

    public static ServerNetwork instance = new ServerNetwork();

    public void init(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerPlayNetworking.registerReceiver(handler, channelID, (server1, player, handler1, buf, responseSender) -> {
            try {
                receiveMessage(buf.array(), player, (INetworkInstance) handler1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    protected UUID getUUIDFromPlayer(PlayerEntity player) {
        return player.getUuid();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, PlayerEntity player) {
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> {
            try {
               if(serverPlayerEntity != player){
                   ServerPlayNetworking.send(serverPlayerEntity, channelID, new PacketByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
               }
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForPlayer(NetData data, PlayerEntity player, UUID target) {
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> {
            if (serverPlayerEntity.getUuid().equals(target)) {
                try {
                    ServerPlayNetworking.send(serverPlayerEntity, channelID, new PacketByteBuf(new PacketByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write()))));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
