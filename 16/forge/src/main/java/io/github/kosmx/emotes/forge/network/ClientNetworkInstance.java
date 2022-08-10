package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientNetworkInstance extends AbstractNetworkInstance {

    boolean isRemotePresent = false;
    private final AtomicInteger connectState = new AtomicInteger(0);

    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init(){
        //ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(ServerNetwork.channelID, this::receiveMessage));
        ServerNetwork.channel.addListener(this::receiveJunk);
        ServerNetwork.channel.addListener(this::registerServerSide);
        MinecraftForge.EVENT_BUS.addListener(this::connectServerCallback);
        MinecraftForge.EVENT_BUS.addListener(this::disconnectEvent);
    }

    private void connectServerCallback(ClientPlayerNetworkEvent.LoggedInEvent event){
        if (connectState.incrementAndGet() == 2) {
            connectState.set(0);
            this.sendConfigCallback();
        }
    }

    private void disconnectEvent(ClientPlayerNetworkEvent.LoggedOutEvent event){
        this.disconnect();
        this.isRemotePresent = false;
        this.connectState.set(0);
    }

    private void receiveJunk(NetworkEvent.ServerCustomPayloadEvent event){
        receiveMessage(event.getPayload());
        event.getSource().get().setPacketHandled(true);
    }

    private void registerServerSide(NetworkEvent.ChannelRegistrationChangeEvent event){
        this.isRemotePresent = event.getRegistrationChangeType() == NetworkEvent.RegistrationChangeType.REGISTER;
        if (isRemotePresent && connectState.incrementAndGet() == 2) {
            connectState.set(0);
            this.sendConfigCallback();
        }
    }

    void receiveMessage(FriendlyByteBuf buf){
        if(buf.isDirect()){
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            receiveMessage(bytes);
        }
        else {
            receiveMessage(buf.array());
        }
    }

    @Override
    public boolean sendPlayerID() {
        return false;
    }

    @Override
    public boolean isActive() {
        return Minecraft.getInstance().getConnection() != null
                && ServerNetwork.channel.isRemotePresent(Minecraft.getInstance().getConnection().getConnection())
                || this.isRemotePresent;
    }

    public static ServerboundCustomPayloadPacket newC2SEmotePacket(NetData data) throws IOException {
        return new ServerboundCustomPayloadPacket(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if(target != null){
            builder.configureTarget(target);
        }
        if(Minecraft.getInstance().getConnection() != null)
            Minecraft.getInstance().getConnection().send(newC2SEmotePacket(builder.copyAndGetData()));
    }
}
