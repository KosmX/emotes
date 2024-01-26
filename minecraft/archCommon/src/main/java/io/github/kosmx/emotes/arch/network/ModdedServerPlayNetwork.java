package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ModdedServerPlayNetwork implements IServerNetworkInstance {
    private final EmotesMixinNetworkAccessor serverGamePacketListener;

    public ModdedServerPlayNetwork(ServerGamePacketListenerImpl serverGamePacketListener) {
        this.serverGamePacketListener = serverGamePacketListener;
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return null;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {

    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {

    }

    @Override
    public void sendConfigCallback() {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public int getRemoteVersion() {
        return 0;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return false;
    }

    @Override
    public int maxDataSize() {
        return 0;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return null;
    }
}
