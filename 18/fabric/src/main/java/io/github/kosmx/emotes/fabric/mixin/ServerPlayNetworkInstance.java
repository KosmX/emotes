package io.github.kosmx.emotes.fabric.mixin;


import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.fabric.network.ServerNetwork;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkInstance implements IServerNetworkInstance {

    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();
    @Shadow public abstract void send(Packet<?> packet);

    HashMap<Byte, Byte> versions = new HashMap<>();
    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return versions;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        versions = map;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return emoteTracker;
    }

    @Override
    public boolean sendPlayerID() {
        return true;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        sendMessage(AbstractNetworkInstance.safeGetBytesFromBuffer(builder.setVersion(getRemoteVersions()).build().write()), null);
    }

    public void sendMessage(byte[] bytes, @Nullable UUID target) {
        this.send(ServerPlayNetworking.createS2CPacket(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))));
    }

    @Override
    public void sendConfigCallback() {
        EmotePacket.Builder builder = new EmotePacket.Builder().configureToConfigExchange(true);
        try{
            this.send(ServerPlayNetworking.createS2CPacket(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(builder.build().write()))));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
