package com.kosmx.emotes.fabric.network;

import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.main.network.IClientNetwork;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

public class ClientNetworkInstance implements IClientNetwork {

    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(ServerNetwork.channelID, this::receiveMessage));
    }

    void receiveMessage(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        if(buf.isDirect()){ //If the received ByteBuf is direct i have to copy that onto the heap
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            receiveMessage(bytes);
        }
        else {
            receiveMessage(buf.array()); //if heap, I can just use it's byte-array
        }
    }

    private boolean disableNBS = false;
    @Override
    public HashMap<Byte, Byte> getVersions() {
        if(disableNBS){
            HashMap<Byte, Byte> map = new HashMap<>();
            map.put((byte)3, (byte) 0);
            return map;
        }
        return null;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        if(map.containsKey((byte)3)){
            disableNBS = map.get((byte)3) == 0;
        }
    }

    @Override
    public boolean sendPlayerID() {
        return false;
    }

    @Override
    public boolean isActive() {
        return ClientPlayNetworking.canSend(ServerNetwork.channelID);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable IEmotePlayerEntity target) throws IOException {
        if(target != null){
            builder.configureTarget(target.getUUID());
        }
        ClientPlayNetworking.send(ServerNetwork.channelID, new PacketByteBuf(Unpooled.wrappedBuffer(builder.build().write().array())));
    }
}
