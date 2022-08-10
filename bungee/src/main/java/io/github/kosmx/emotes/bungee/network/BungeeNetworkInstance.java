package io.github.kosmx.emotes.bungee.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class BungeeNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    private HashMap<Byte, Byte> version = null;
    final ProxiedPlayer player;

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    public BungeeNetworkInstance(ProxiedPlayer player) {
        this.player = player;
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
        player.sendData(CommonData.getIDAsString(CommonData.playEmoteID), bytes);
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