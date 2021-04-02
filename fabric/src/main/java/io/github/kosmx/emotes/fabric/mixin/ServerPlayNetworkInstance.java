package io.github.kosmx.emotes.fabric.mixin;


import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.fabric.network.ServerNetwork;
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

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkInstance implements INetworkInstance {
    @Shadow public abstract void send(Packet<?> packet);

    HashMap<Byte, Byte> versions = new HashMap<>();
    @Override
    public HashMap<Byte, Byte> getVersions() {
        return versions;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        if(map.containsKey((byte)3)) {
            versions.put((byte) 3, map.get((byte) 3));
        }
    }

    @Override
    public boolean sendPlayerID() {
        return true;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable IEmotePlayerEntity target) throws IOException {
        sendMessage(builder.build().write(), null);
    }

    @Override
    public void sendMessage(byte[] bytes, @Nullable IEmotePlayerEntity target) {
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
        return false;
    }
}
