package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.forge.mixin.ChunkMapAccessor;
import io.github.kosmx.emotes.forge.mixin.TrackedEntityAccessor;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServerNetwork extends AbstractServerEmotePlay<Player> {
    public static final ResourceLocation channelID = new ResourceLocation(CommonData.MOD_ID, CommonData.playEmoteID);

    public static final ResourceLocation geyserChannelID = new ResourceLocation("geyser", "emote");

    public static final EventNetworkChannel channel = NetworkRegistry.newEventChannel(
            channelID,
            () -> "8",
            s -> true,
            s -> true
    );

    public static final EventNetworkChannel geyserChannel = NetworkRegistry.newEventChannel(
            geyserChannelID,
            () -> "0",
            s -> true,
            s -> true
    );

    public static ServerNetwork instance = new ServerNetwork();

    public void init(){
        channel.addListener(this::receiveByteBuf);
        geyserChannel.addListener(networkEvent -> receiveGeyserMessage(networkEvent.getSource().get().getSender(), toBytes(networkEvent.getPayload())));
    }

    public void receiveByteBuf(NetworkEvent.ClientCustomPayloadEvent event){
        instance.receiveMessage(event.getSource().get().getSender(), event.getSource().get().getSender().connection, event.getPayload());
        event.getSource().get().setPacketHandled(true);//it was handled just in a bit weirder way me :D
    }

    void receiveMessage(ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf) {
        try {
            receiveMessage(toBytes(buf), player, (INetworkInstance) handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte[] toBytes(FriendlyByteBuf buf){
        if(buf.isDirect()){
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            return bytes;
        }
        else {
            return buf.array();
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getId();
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        /* I don't want to use this *shit* packet distributor. maybe tomorrow.*/

        try {
            sendConsumer(player, serverPlayer -> {
                try {
                    if (geyserChannel.isRemotePresent(serverPlayer.connection.getConnection())) {
                        PacketDistributor.PLAYER.with(() -> serverPlayer).send(newS2CEmotesPacket(geyserChannelID, packet.write()));
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            });
        }catch (Throwable e){
            e.printStackTrace();
        }

         //*/
    }

    @Override
    protected void sendForEveryoneElse(NetData data, @Nullable GeyserEmotePacket emotePacket, Player player) {
        data.player = player.getUUID();
        try {
            sendConsumer(player, (Consumer<ServerPlayer>) serverPlayer -> {
                try {
                    PacketDistributor.PLAYER.with(() -> serverPlayer).send(newS2CEmotesPacket(data));
                    if (emotePacket != null && geyserChannel.isRemotePresent(serverPlayer.connection.getConnection())) {
                        PacketDistributor.PLAYER.with(() -> serverPlayer).send(newS2CEmotesPacket(geyserChannelID, emotePacket.write()));
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Packet newS2CEmotesPacket(NetData data) throws IOException {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket();
        packet.setName(channelID);
        packet.setData(new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
        return packet;//:D
    }

    public static Packet newS2CEmotesPacket(ResourceLocation channelID, byte[] data) throws IOException {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket();
        packet.setName(channelID);
        packet.setData(new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
        return packet;
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        if(Objects.requireNonNull(player.getCommandSenderWorld().getPlayerByUUID(target)).canSee(player)){
            sendForPlayer(data, player, target);
        }
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

    private void sendConsumer(Player player, Consumer<ServerPlayer> consumer){
        TrackedEntityAccessor tracker = ((ChunkMapAccessor)((ServerChunkCache)player.getCommandSenderWorld().getChunkSource()).chunkMap).getTrackedEntity().get(player.getId());
        tracker.getPlayersTracking().stream().forEach(consumer);
    }
}
