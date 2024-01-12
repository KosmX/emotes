package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.forge.network.packets.ForgeMainEmotePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientNetworkInstance extends AbstractNetworkInstance {

    boolean isRemotePresent = false;
    private final AtomicInteger connectState = new AtomicInteger(0);

    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init() {
        // ServerNetwork.channel.addListener(this::registerServerSide);

        registerServerSide(); // TODO

        NeoForge.EVENT_BUS.addListener(this::connectServerCallback);
        NeoForge.EVENT_BUS.addListener(this::disconnectEvent);
    }

    private void connectServerCallback(ClientPlayerNetworkEvent.LoggingIn event){
        if (connectState.incrementAndGet() == 2) {
            connectState.set(0);
            this.sendConfigCallback();
        }
    }

    private void disconnectEvent(ClientPlayerNetworkEvent.LoggingOut event){
        this.disconnect();
        this.isRemotePresent = false;
        this.connectState.set(0);
    }

    protected void receiveJunk(ForgeMainEmotePacket forgeMainEmotePacket, PlayPayloadContext playPayloadContext) {
        receiveMessage(forgeMainEmotePacket.byteBuf());
    }

    private void registerServerSide() {
        this.isRemotePresent = true;
        if (isRemotePresent && connectState.incrementAndGet() == 2) {
            connectState.set(0);
            this.sendConfigCallback();
        }
    }

    void receiveMessage(ByteBuf buf){
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
                && Minecraft.getInstance().getConnection().isConnected(ForgeMainEmotePacket.channelID)
                || this.isRemotePresent;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if(target != null){
            builder.configureTarget(target);
        }

        PacketDistributor.SERVER.noArg().send(ForgeMainEmotePacket.newEmotePacket(builder.copyAndGetData()));
    }
}
