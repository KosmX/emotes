package io.github.kosmx.emotes.fabric.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.fabric.FabricWrapper;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
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
import java.util.Objects;
import java.util.UUID;


public class ServerNetwork extends AbstractServerEmotePlay<Player> {
    public static final ResourceLocation channelID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);
    public static final ResourceLocation geyserChannelID = new ResourceLocation("geyser", "emote");

    public static ServerNetwork instance = new ServerNetwork();

    public void init(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.registerReceiver(handler, channelID, this::receiveMessage);
            ServerPlayNetworking.registerReceiver(handler, geyserChannelID, this::receiveGeyserMessage);
        });
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

    void receiveGeyserMessage(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
        if(buf.isDirect()){
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            receiveGeyserMessage(player, bytes);
        }
        else {
            receiveGeyserMessage(player, buf.array());
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected Player getPlayerFromUUID(UUID player) {
        return FabricWrapper.SERVER_INSTANCE.getPlayerList().getPlayer(player);
    }


    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getId();
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player player) {
        return (IServerNetworkInstance) ((ServerPlayer)player).connection; //If the mixin works, this should suffice//
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        PlayerLookup.tracking(player).forEach(serverPlayer -> {
            try {
                if (serverPlayer != player && ServerPlayNetworking.canSend(serverPlayer, geyserChannelID)){
                    ServerPlayNetworking.send(serverPlayer, geyserChannelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.write())));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForEveryoneElse(NetData data, GeyserEmotePacket emotePacket, Player player) {
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> {
            try {
                if (serverPlayerEntity != player) {
                    if (ServerPlayNetworking.canSend(serverPlayerEntity, channelID)) {
                        EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
                        packetBuilder.setVersion(((IServerNetworkInstance)serverPlayerEntity.connection).getRemoteVersions());
                        ServerPlayNetworking.send(serverPlayerEntity, channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(packetBuilder.build().write().array())));
                    }
                    else if (ServerPlayNetworking.canSend(serverPlayerEntity, geyserChannelID) && emotePacket != null)
                        ServerPlayNetworking.send(serverPlayerEntity, geyserChannelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(emotePacket.write())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        PlayerLookup.tracking(player).forEach(serverPlayerEntity -> targetFinder(serverPlayerEntity, data, target));
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        PlayerLookup.all(Objects.requireNonNull(player.getServer())).forEach(serverPlayerEntity -> targetFinder(serverPlayerEntity, data, target));
    }

    private void targetFinder(ServerPlayer serverPlayerEntity, NetData data, UUID target){
        if (serverPlayerEntity.getUUID().equals(target)) {
            try {
                EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
                packetBuilder.setVersion(((IServerNetworkInstance)serverPlayerEntity.connection).getRemoteVersions());
                ServerPlayNetworking.send(serverPlayerEntity, channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(packetBuilder.build().write().array())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
