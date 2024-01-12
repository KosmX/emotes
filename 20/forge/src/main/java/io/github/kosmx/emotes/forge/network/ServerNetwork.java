package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.forge.mixin.ChunkMapAccessor;
import io.github.kosmx.emotes.forge.mixin.TrackedEntityAccessor;
import io.github.kosmx.emotes.forge.network.packets.ForgeGeyserEmotePacket;
import io.github.kosmx.emotes.forge.network.packets.ForgeMainEmotePacket;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;


public class ServerNetwork extends AbstractServerEmotePlay<Player> {
    public static ServerNetwork instance = new ServerNetwork();

    public void init(){

    }

    @SubscribeEvent
    public void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("emotecraft")
                .optional();

        registrar.play(ForgeMainEmotePacket.channelID, ForgeMainEmotePacket::new, handler -> handler
                .client(ClientNetworkInstance.networkInstance::receiveJunk)
                .server(this::receiveByteBuf));

        registrar.play(ForgeGeyserEmotePacket.geyserChannelID, ForgeGeyserEmotePacket::new, handler -> handler
                .server(this::receiveGeyserEvent));
    }

    public void receiveByteBuf(ForgeMainEmotePacket event, PlayPayloadContext context) {
        try{
            Player player = context.player().orElse(null);
            ByteBuf buf = event.byteBuf();

            if(buf.isDirect()){
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                receiveMessage(bytes, player, getPlayerNetworkInstance(player));
            } else {
                receiveMessage(buf.array(), player, getPlayerNetworkInstance(player));
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void receiveGeyserEvent(ForgeGeyserEmotePacket event, PlayPayloadContext context) {
        try{
            Player player = context.player().orElse(null);
            ByteBuf buf = event.byteBuf();

            if(buf.isDirect()){
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                receiveGeyserMessage(player, bytes);
            } else {
                receiveGeyserMessage(player, buf.array());
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUUID();
    }

    @Override
    protected Player getPlayerFromUUID(UUID player) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player);
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
        sendConsumer(player, serverPlayer -> {
            try {
                if (serverPlayer != player && serverPlayer.connection.isConnected(ForgeGeyserEmotePacket.geyserChannelID)) {
                    PacketDistributor.PLAYER.with(serverPlayer).send(new ForgeGeyserEmotePacket(Unpooled.wrappedBuffer(packet.write())));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForEveryoneElse(NetData data, GeyserEmotePacket emotePacket, Player player) {
        data.player = player.getUUID();
        sendConsumer(player, serverPlayerEntity -> {
            try {
                if (serverPlayerEntity != player) {
                    if (serverPlayerEntity.connection.isConnected(ForgeMainEmotePacket.channelID)) {
                        EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
                        packetBuilder.setVersion(((IServerNetworkInstance)serverPlayerEntity.connection).getRemoteVersions());


                        PacketDistributor.PLAYER.with(serverPlayerEntity).send(new ForgeMainEmotePacket(Unpooled.wrappedBuffer(packetBuilder.build().write().array())));
                    }
                    else if (serverPlayerEntity.connection.isConnected(ForgeGeyserEmotePacket.geyserChannelID) && emotePacket != null)
                        PacketDistributor.PLAYER.with(serverPlayerEntity).send(new ForgeGeyserEmotePacket(Unpooled.wrappedBuffer(emotePacket.write())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        Player destination = player.getCommandSenderWorld().getPlayerByUUID(target);

        if(destination != null) {
            sendConsumer(player, serverPlayer -> {
                if(serverPlayer == destination){
                    sendForPlayer(data, player, target);
                }
            });
        }
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        ServerPlayer serverPlayerEntity = (ServerPlayer) player.getCommandSenderWorld().getPlayerByUUID(target);

        if (serverPlayerEntity == null)
            return;

        try {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
            packetBuilder.setVersion(((IServerNetworkInstance)serverPlayerEntity.connection).getRemoteVersions());

            PacketDistributor.PLAYER.with(serverPlayerEntity)
                    .send(new ForgeMainEmotePacket(Unpooled.wrappedBuffer(packetBuilder.build().write().array())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendConsumer(Player player, Consumer<ServerPlayer> consumer){
        TrackedEntityAccessor tracker = ((ChunkMapAccessor)((ServerChunkCache)player.getCommandSenderWorld().getChunkSource()).chunkMap).getTrackedEntity().get(player.getId());
        if (tracker != null) {
            tracker.getPlayersTracking().forEach(serverPlayerConnection -> consumer.accept(serverPlayerConnection.getPlayer()));
        }
    }
}
