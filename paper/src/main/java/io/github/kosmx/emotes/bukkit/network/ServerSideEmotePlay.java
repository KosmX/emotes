package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public final class ServerSideEmotePlay extends AbstractServerEmotePlay<BukkitNetworkInstance> implements Listener {
    private final BukkitWrapper plugin = BukkitWrapper.getPlugin(BukkitWrapper.class);;
    final HashMap<UUID, BukkitNetworkInstance> player_database = new HashMap<>();

    public void receivePluginMessage(String channel, Player player, byte[] message) {
        //EmoteInstance.instance.getLogger().log(Level.FINE, "[EMOTECRAFT] streaming emote");
        if (channel.equals(BukkitWrapper.EmotePacket)) {
            BukkitNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
            if (playerNetwork != null) {
                //Let the common server logic process the message
                try {
                    this.receiveMessage(message, playerNetwork);
                } catch (Exception e) {
                    LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                }
            } else {
                LoggerService.INSTANCE.log(Level.WARNING, "Player: " + player.getName() + " is not registered");
            }
        }
    }

    @Override
    public UUID getUUIDFromPlayer(BukkitNetworkInstance player) {
        return player.player.getUniqueId();
    }

    public BukkitNetworkInstance getPlayerNetworkInstance(Player player) {
        return getPlayerFromUUID(player.getUniqueId());
    }

    @Override
    public BukkitNetworkInstance getPlayerFromUUID(UUID playerUuid) {
        if (!player_database.containsKey(playerUuid)) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            assert player != null;
            LoggerService.INSTANCE.log(Level.INFO, "Player " + player.getName() + " never joined. If it is a fake player, the fake-player plugin forgot to fire join event.");
            player_database.put(playerUuid, new BukkitNetworkInstance(player));
        }
        return player_database.get(playerUuid);
    }

    @Override
    protected void sendForEveryoneElse(NetData data, BukkitNetworkInstance player) {
        for (Player player1 : player.player.getTrackedBy()) {
            BukkitNetworkInstance instance = getPlayerNetworkInstance(player1);
            if (instance == player) continue;

            // Bukkit server will filter if I really can send, or not.
            // If else to not spam dumb forge clients.
            if (player1.getListeningPluginChannels().contains(BukkitWrapper.EmotePacket)) {
                sendForPlayer(data, player, instance);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.player_database.put(event.getPlayer().getUniqueId(), new BukkitNetworkInstance(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        BukkitNetworkInstance instance = this.player_database.remove(event.getPlayer().getUniqueId());
        if (instance != null) instance.closeConnection();
    }

    @EventHandler
    public void onPlayerTrackEntity(PlayerTrackEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            playerStartTracking(getPlayerNetworkInstance(player), getPlayerNetworkInstance(event.getPlayer()));
        }
    }

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    public static ServerSideEmotePlay getInstance() {
        return (ServerSideEmotePlay) AbstractServerEmotePlay.getInstance();
    }
}
