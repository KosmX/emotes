package io.github.kosmx.emotes.velocity.network;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.github.kosmx.emotes.velocity.VelocityWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ServerSideEmotePlay extends AbstractServerEmotePlay<Player> {

    private final ProxyServer server;
    private final HashMap<UUID, VelocityNetworkInstance> player_database = new HashMap<>();

    public ServerSideEmotePlay(ProxyServer server) {
        this.server = server;
        server.getChannelRegistrar().register(VelocityWrapper.EmotePacket);
        server.getChannelRegistrar().register(VelocityWrapper.GeyserPacket);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) {
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (event.getIdentifier().equals(VelocityWrapper.EmotePacket)) {
            if (event.getSource() instanceof Player) {
                Player player = (Player) event.getSource();

                VelocityNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
                if (playerNetwork != null) {
                    // Let the common server logic process the message
                    try {
                        this.receiveMessage(event.getData(), player, playerNetwork);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player.getUsername() + " is not registered");
                }
            } else {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Uhh oh, sender is not a player");
            }
        }
        else if (event.getIdentifier().equals(VelocityWrapper.GeyserPacket)) {
            if (event.getSource() instanceof Player) {
                Player player = (Player) event.getSource();

                receiveGeyserMessage(player, event.getData());
            } else {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Uhh oh, sender is not a player");
            }
        }
        else {
            event.setResult(PluginMessageEvent.ForwardResult.forward());
        }
    }

    @Subscribe
    public void join(PostLoginEvent event) {
        this.player_database.put(event.getPlayer().getUniqueId(), new VelocityNetworkInstance(event.getPlayer()));
    }

    @Subscribe
    public void leave(DisconnectEvent event) {
        Player player = event.getPlayer();

        VelocityNetworkInstance instance = this.player_database.remove(player.getUniqueId());
        if (instance != null)
            instance.closeConnection();
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    protected Player getPlayerFromUUID(UUID player) {
        return server.getPlayer(player).orElse(null);
    }

    @Override
    protected long getRuntimePlayerID(Player player) {
        return player.getUniqueId().getMostSignificantBits() & Long.MAX_VALUE;
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player player) {
        return player_database.get(getUUIDFromPlayer(player));
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        for (Player player1 : server.getAllPlayers()) {
            if (player1 != player) {
                try {
                    player1.sendPluginMessage(VelocityWrapper.GeyserPacket, packet.write());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForEveryoneElse(NetData data, @Nullable GeyserEmotePacket emotePacket, Player player) {
        for (Player player1 : server.getAllPlayers()) {
            if (player1 != player) {
                try {
                    player1.sendPluginMessage(VelocityWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        sendForPlayer(data, player, target);
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        Player targetPlayer = getPlayerFromUUID(target);
        try {
            targetPlayer.sendPluginMessage(VelocityWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
