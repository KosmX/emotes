package io.github.kosmx.emotes.velocity.network;

import com.velocitypowered.api.proxy.Player;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.github.kosmx.emotes.velocity.VelocityWrapper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class VelocityNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {

    private HashMap<Byte, Byte> version = null;
    private final Player player;

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    public VelocityNetworkInstance(Player player) {
        this.player = player;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return version;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        this.version = map;
    }

    @Override
    public void sendMessage(byte[] bytes, @Nullable UUID target) {
        player.sendPluginMessage(VelocityWrapper.EmotePacket, bytes);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean trackPlayState() {
        return false;
    }

}
