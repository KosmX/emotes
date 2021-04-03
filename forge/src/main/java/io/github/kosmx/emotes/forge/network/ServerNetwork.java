package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.server.AbstractServerEmotePlay;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkInstance;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import java.io.IOException;
import java.util.UUID;

public class ServerNetwork extends AbstractServerEmotePlay<Player> {
    public static final ResourceLocation channelID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);

    public static final EventNetworkChannel channel = NetworkRegistry.newEventChannel(
            channelID,
            () -> "8",
            s -> true,
            s -> true
    );

    public static ServerNetwork instance = new ServerNetwork();

    public void init(){
        channel.addListener(this::receiveByteBuf);
    }

    @SubscribeEvent
    private void receiveByteBuf(NetworkEvent event){
        receiveMessage(event.getSource().get().getSender(), event.getSource().get().getSender().connection, event.getPayload());
    }

    void receiveMessage(ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf){
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
        try{
        PacketDistributor.TRACKING_ENTITY.with(()->player).send(newS2CEmotesPacket(data));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Packet newS2CEmotesPacket(NetData data) throws IOException {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket();
        packet.setName(channelID);
        packet.setData(new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
        return packet;//:D
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        try {
            PacketDistributor.PLAYER.with(() -> (ServerPlayer) player.getCommandSenderWorld().getPlayerByUUID(target)).send(newS2CEmotesPacket(data));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
