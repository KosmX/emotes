package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ClientNetworkInstance extends AbstractNetworkInstance implements C2SPlayChannelEvents.Register, ClientPlayConnectionEvents.Disconnect {


    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(ServerNetwork.channelID, this::receiveMessage));
        C2SPlayChannelEvents.REGISTER.register(this);

        ClientPlayConnectionEvents.DISCONNECT.register(this);
    }

    @Override
    public void onChannelRegister(ClientPacketListener handler, PacketSender sender, Minecraft client, List<ResourceLocation> channels) {
        if(channels.contains(ServerNetwork.channelID)){
            this.sendConfigCallback();
            EmoteInstance.instance.getLogger().log(Level.INFO, "Sending presence to server");
        }
    }
    @Override
    public void onPlayDisconnect(ClientPacketListener handler, Minecraft client) {
        this.disconnect(); //:D
    }

    void receiveMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender){
        if(buf.isDirect() || buf.isReadOnly()){ //If the received ByteBuf is direct i have to copy that onto the heap
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
    public boolean sendPlayerID() {
        return false;
    }

    @Override
    public boolean isActive() {
        return ClientPlayNetworking.canSend(ServerNetwork.channelID);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if(target != null){
            builder.configureTarget(target);
        }
        EmotePacket writer = builder.build();
        ClientPlayNetworking.send(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(writer.write().array())));
        if(writer.data.emoteData != null && writer.data.emoteData.song != null && !writer.data.writeSong){
            EmoteInstance.instance.getClientMethods().sendChatMessage(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.song_too_big_to_send"));
        }
    }
}
