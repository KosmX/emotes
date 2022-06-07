package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
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


    public ServerSideEmotePlay(BukkitWrapper plugin){
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BukkitWrapper.EmotePacket);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BukkitWrapper.GeyserPacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BukkitWrapper.EmotePacket, this::receivePluginMessage);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BukkitWrapper.GeyserPacket, this::receivePluginMessage);
    }

    private void receivePluginMessage(String channel, Player player, byte[] message) {
        //EmoteInstance.instance.getLogger().log(Level.FINE, "[EMOTECRAFT] streaming emote");
        if (channel.equals(BukkitWrapper.EmotePacket)) {
            BukkitNetworkInstance playerNetwork = player_database.getOrDefault(player.getUniqueId(), null);
            if (playerNetwork != null) {
                //Let the common server logic process the message
                try {
                    this.receiveMessage(message, player, playerNetwork);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player.getName() + " is not registered");
            }
        }
        else {
            receiveGeyserMessage(player, message);
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
    protected long getRuntimePlayerID(Player player) {
        return player.getEntityId();
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(Player player) {
        return player_database.get(getUUIDFromPlayer(player));
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) {
        return this.player_database.get(player);
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    player1.sendPluginMessage(plugin, BukkitWrapper.GeyserPacket, packet.write());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void sendForEveryoneElse(NetData data, GeyserEmotePacket emotePacket, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    //Bukkit server will filter if I really can send, or not.
                    //If else to not spam dumb forge clients.
                    if(player1.getListeningPluginChannels().contains(BukkitWrapper.EmotePacket))
                        player1.sendPluginMessage(plugin, BukkitWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
                    else if(emotePacket != null) player1.sendPluginMessage(plugin, BukkitWrapper.GeyserPacket, emotePacket.write());
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
            targetPlayer.sendPluginMessage(plugin, BukkitWrapper.EmotePacket, new EmotePacket.Builder(data).build().write().array());
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
