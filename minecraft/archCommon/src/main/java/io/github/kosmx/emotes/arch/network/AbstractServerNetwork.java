package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractServerNetwork implements INetworkInstance, IServerNetworkInstance {
    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    @NotNull
    protected abstract EmotesMixinConnection getServerConnection();

    @NotNull
    protected abstract Avatar getAvatar();

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return getServerConnection().emotecraft$getRemoteVersions();
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        getServerConnection().emotecraft$setVersions(map);
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return true; // MC server does track this
    }

    @Override
    public int maxDataSize() {
        return CommonData.MAX_PACKET_SIZE - 16; // channel ID is 12, one extra int makes it 16 (string)
    }

    @Override
    public boolean isActive() {
        return getAvatar() instanceof ServerPlayer;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        sendPlayMessage(builder.setVersion(getRemoteVersions()).build().write());
    }

    public abstract void sendPlayMessage(ByteBuffer bytes);
}
