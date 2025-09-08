package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BukkitNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();
    protected final Player player;

    public BukkitNetworkInstance(Player player) {
        this.player = player;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    @Override
    public void sendMessage(byte[] bytes, @Nullable UUID target) {
        this.player.sendPluginMessage(PLUGIN, BukkitWrapper.EMOTE_PACKET, bytes);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void presenceResponse() {
        super.presenceResponse();
        ServerSideEmotePlay.getInstance().presenceResponse(this, trackPlayState());
    }
}
