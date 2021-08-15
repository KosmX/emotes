package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ServerSideEmotePlay extends AbstractServerEmotePlay<Player> implements Listener {
    final BukkitWrapper plugin;

    final HashMap<UUID, BukkitNetworkInstance> player_database = new HashMap<>();

    public static ServerSideEmotePlay INSTANCE;

    public ServerSideEmotePlay(BukkitWrapper plugin){
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BukkitWrapper.Emotepacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BukkitWrapper.Emotepacket, this::receivePluginMessage);
    }

    private void receivePluginMessage(String ignore, Player player, byte[] message) {
        EmoteInstance.instance.getLogger().log(Level.FINE, "[EMOTECRAFT] streaming emote");
        BukkitNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
        if(playerNetwork != null){
            //Let the common server logic process the message
            try {
                this.receiveMessage(message, player, playerNetwork);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player.getName() + " is not registered");
        }
    }

    @Override
    protected UUID getUUIDFromPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    protected void sendForEveryoneElse(NetData data, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    player1.sendPluginMessage(plugin, BukkitWrapper.Emotepacket, new EmotePacket.Builder(data).build().write().array());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        if(targetPlayer.canSee(player)){
            sendForPlayer(data, player, target);
        }
    }

    @Override
    protected void sendForPlayer(NetData data, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        try {
            targetPlayer.sendPluginMessage(plugin, BukkitWrapper.Emotepacket, new EmotePacket.Builder(data).build().write().array());
        }catch (Exception e){
            e.printStackTrace();
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
}
