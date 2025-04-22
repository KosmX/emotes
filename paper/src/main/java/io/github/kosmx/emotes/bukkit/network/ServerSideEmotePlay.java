package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public final class ServerSideEmotePlay extends AbstractServerEmotePlay<Player> implements Listener {
    private final BukkitWrapper plugin = BukkitWrapper.getPlugin(BukkitWrapper.class);;
    final HashMap<UUID, BukkitNetworkInstance> player_database = new HashMap<>();

    public void receivePluginMessage(String channel, Player player, byte[] message) {
        //EmoteInstance.instance.getLogger().log(Level.FINE, "[EMOTECRAFT] streaming emote");
        if (channel.equals(BukkitWrapper.EmotePacket)) {
            BukkitNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
            if (playerNetwork != null) {
                //Let the common server logic process the message
                try {
                    this.receiveMessage(message, player, playerNetwork);
                } catch (Exception e) {
                    LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                }
            } else {
                LoggerService.INSTANCE.log(Level.WARNING, "Player: " + player.getName() + " is not registered");
            }
        }
    }

    @Override
    public UUID getUUIDFromPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    public Player getPlayerFromUUID(UUID player) {
        return plugin.getServer().getPlayer(player);
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player player) {
        UUID playerUuid = getUUIDFromPlayer(player);
        if (!player_database.containsKey(playerUuid)) {
            LoggerService.INSTANCE.log(Level.INFO, "Player " + player.getName() + " never joined. If it is a fake player, the fake-player plugin forgot to fire join event.");
            player_database.put(playerUuid, new BukkitNetworkInstance(player));
        }
        return player_database.get(playerUuid);
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) {
        if (!player_database.containsKey(player)) return getPlayerNetworkInstance(getPlayerFromUUID(player));
        return this.player_database.get(player);
    }

    @Override
    protected void sendForEveryoneElse(NetData data, Player player) {
        for(Player player1 : player.getTrackedBy()){
            if (player1 != player) {
                try {
                    //Bukkit server will filter if I really can send, or not.
                    //If else to not spam dumb forge clients.
                    if(player1.getListeningPluginChannels().contains(BukkitWrapper.EmotePacket)) {
                        EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data.copy());
                        packetBuilder.setVersion(getPlayerNetworkInstance(player1).getRemoteVersions());
                        player1.sendPluginMessage(plugin, BukkitWrapper.EmotePacket, packetBuilder.build().write().array());
                    }
                }catch (Exception e){
                    LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        if (targetPlayer == null) return;
        if (player.isTrackedBy(targetPlayer)) {
            sendForPlayer(data, player, target);
        }
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        try {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data.copy());
            packetBuilder.setVersion(getPlayerNetworkInstance(targetPlayer).getRemoteVersions());
            targetPlayer.sendPluginMessage(plugin, BukkitWrapper.EmotePacket, packetBuilder.build().write().array());
        }catch (Exception e){
            LoggerService.INSTANCE.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        this.player_database.put(event.getPlayer().getUniqueId(), new BukkitNetworkInstance(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();

        BukkitNetworkInstance instance = this.player_database.remove(player.getUniqueId());
        if(instance != null)instance.closeConnection();
    }

    @EventHandler
    public void onPlayerTrackEntity(PlayerTrackEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            playerStartTracking(player, event.getPlayer());
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
