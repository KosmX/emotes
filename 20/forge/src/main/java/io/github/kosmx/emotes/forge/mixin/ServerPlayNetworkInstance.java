package io.github.kosmx.emotes.forge.mixin;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.forge.network.ServerNetwork;
import io.github.kosmx.emotes.forge.network.packets.ForgeMainEmotePacket;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkInstance implements IServerNetworkInstance {
    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();

    @Shadow public ServerPlayer player;

    @Shadow public abstract ServerPlayer getPlayer();

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
        PacketDistributor.PLAYER.with(this.player).send(ForgeMainEmotePacket.newEmotePacket(builder.copyAndGetData()));
    }

    @Override
    public void sendConfigCallback() {
        try {
            PacketDistributor.PLAYER.with(this.player)
                    .send(ForgeMainEmotePacket.newEmotePacket(
                            new EmotePacket.Builder()
                                    .configureToConfigExchange(true)
                                    .copyAndGetData()
                    ));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void presenceResponse() {
        IServerNetworkInstance.super.presenceResponse();
        ServerNetwork.sendConsumer(this.getPlayer(), otherPlayer -> ServerNetwork.getInstance().playerStartTracking(otherPlayer, ServerPlayNetworkInstance.this.player));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
