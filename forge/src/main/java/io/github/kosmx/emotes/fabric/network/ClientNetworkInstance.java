package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.network.IClientNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

public class ClientNetworkInstance implements IClientNetwork {

    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(ServerNetwork.channelID, this::receiveMessage));
    }

    void receiveMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender){
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
            builder.configureTarget(target.emotes_getUUID());
        }
        ClientPlayNetworking.send(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(builder.build().write().array())));
    }
}
