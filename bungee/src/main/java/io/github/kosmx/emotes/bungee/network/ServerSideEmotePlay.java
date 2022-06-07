package io.github.kosmx.emotes.bungee.network;

import io.github.kosmx.emotes.bungee.BungeeWrapper;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ServerSideEmotePlay extends AbstractServerEmotePlay<ProxiedPlayer> implements Listener {
    final BungeeWrapper plugin;

    final HashMap<UUID, BungeeNetworkInstance> player_database = new HashMap<>();

    public static ServerSideEmotePlay INSTANCE;

    public ServerSideEmotePlay(BungeeWrapper plugin) {
        this.plugin = plugin;
        plugin.getProxy().registerChannel(BungeeWrapper.EmotePacket);
        plugin.getProxy().registerChannel(BungeeWrapper.GeyserPacket);
    }

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) {
        EmoteInstance.instance.getLogger().log(Level.FINE, "[EMOTECRAFT] streaming emote");
        if (event.getTag().equals(BungeeWrapper.EmotePacket)) {
            if (event.getSender() instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) event.getSender();

                BungeeNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
                if (playerNetwork != null) {
                    // Let the common server logic process the message
                    try {
                        this.receiveMessage(event.getData(), player, playerNetwork);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player.getName() + " is not registered");
                }
            } else {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Uhh oh, sender is not a player");
            }
        } else if (event.getTag().equals(BungeeWrapper.GeyserPacket)) {
            if (event.getSender() instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) event.getSender();

                receiveGeyserMessage(player, event.getData());
            } else {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Uhh oh, sender is not a player");
            }
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(ProxiedPlayer player) {
        return player.getUniqueId();
    }

    @Override
    protected ProxiedPlayer getPlayerFromUUID(UUID player) {
        return plugin.getProxy().getPlayer(player);
    }

    @Override
    protected long getRuntimePlayerID(ProxiedPlayer player) {
        return player.getUniqueId().getMostSignificantBits() & Long.MAX_VALUE;
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(ProxiedPlayer player) {
        return player_database.get(getUUIDFromPlayer(player));
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) {
        return this.player_database.get(player);
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, ProxiedPlayer player) {
        for (ProxiedPlayer player1 : plugin.getProxy().getPlayers()) {
            if (player1 != player) {
                try {
                    player1.sendData(BungeeWrapper.GeyserPacket, packet.write());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForEveryoneElse(NetData data, GeyserEmotePacket emotePacket, ProxiedPlayer player) {
        for (ProxiedPlayer player1 : plugin.getProxy().getPlayers()) {
            if (player1 != player) {
                try {
                    player1.sendData(BungeeWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, ProxiedPlayer player, UUID target) {
        sendForPlayer(data, player, target);
    }

    @Override
    protected void sendForPlayer(NetData data, ProxiedPlayer player, UUID target) {
        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(target);
        try {
            targetPlayer.sendData(BungeeWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        this.player_database.put(event.getPlayer().getUniqueId(), new BungeeNetworkInstance(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        BungeeNetworkInstance instance = this.player_database.remove(player.getUniqueId());
        if (instance != null)
            instance.closeConnection();
    }
}