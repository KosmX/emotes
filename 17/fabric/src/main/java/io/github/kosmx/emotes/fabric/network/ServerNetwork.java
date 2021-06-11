package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.server.AbstractServerEmotePlay;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import java.io.IOException;
import java.util.UUID;


public class ServerNetwork extends AbstractServerEmotePlay<Player> {
    public static final ResourceLocation channelID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);

    public static ServerNetwork instance = new ServerNetwork();

    public void init(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerPlayNetworking.registerReceiver(handler, channelID, this::receiveMessage));
    }

    void receiveMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
        try{
            if(buf.isDirect()){
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                receiveMessage(bytes, player, (INetworkInstance) handler);
            }
            else {
                receiveMessage(buf.array(), player, (INetworkInstance) handler);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, Player player) {
        data.player = player.getUUID();
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> {
            try {
               if(serverPlayerEntity != player){
                   ServerPlayNetworking.send(serverPlayerEntity, channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
               }
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> {
            if (serverPlayerEntity.getUUID().equals(target)) {
                try {
                    ServerPlayNetworking.send(serverPlayerEntity, channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
