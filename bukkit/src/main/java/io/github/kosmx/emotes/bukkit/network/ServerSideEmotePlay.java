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
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import java.util.UUID;
import java.util.logging.Level;

public class ServerSideEmotePlay extends AbstractServerEmotePlay<Player> implements Listener {
    final BukkitWrapper plugin;

    //final HashMap<UUID, BukkitNetworkInstance> player_database = new HashMap<>();//TODO HAS MODE to Abstract
    /**{@link AbstractServerEmotePlay#player_database}*/

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
            BukkitNetworkInstance playerNetwork = (BukkitNetworkInstance) player_database.getOrDefault(player.getUniqueId(), null);
            if (playerNetwork != null) {
                //Let the common server logic process the message
                try {
                    this.receiveMessage(message, player, playerNetwork);
                } catch (Exception e) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
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
        UUID playerUuid = getUUIDFromPlayer(player);
        if (!player_database.containsKey(playerUuid)) {
            EmoteInstance.instance.getLogger().log(Level.INFO, "Player " + player.getName() + " never joined. If it is a fake player, the fake-player plugin forgot to fire join event.");
            player_database.put(playerUuid, new BukkitNetworkInstance(player));
        }
        return (IServerNetworkInstance) player_database.get(playerUuid);
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) {
        if (!player_database.containsKey(player)) return getPlayerNetworkInstance(getPlayerFromUUID(player));
        return (IServerNetworkInstance) this.player_database.get(player);
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, Player player) {
        for(Player player1 : plugin.getServer().getOnlinePlayers()){
            if (player1 != player && player1.canSee(player)) {
                try {
                    player1.sendPluginMessage(plugin, BukkitWrapper.GeyserPacket, packet.write());
                }catch (Exception e){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
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
                    if(player_database.containsKey(player1.getUniqueId())//TODO HAS MODE, first check in database?
                            || player1.getListeningPluginChannels().contains(BukkitWrapper.EmotePacket)) {
                        EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data.copy());
                        packetBuilder.setVersion(getPlayerNetworkInstance(player1).getRemoteVersions());
                        player1.sendPluginMessage(plugin, BukkitWrapper.EmotePacket, packetBuilder.build().write().array());
                    }
                    else if(emotePacket != null) player1.sendPluginMessage(plugin, BukkitWrapper.GeyserPacket, emotePacket.write());
                }catch (Exception e){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void sendForPlayerInRange(NetData data, Player player, UUID target) {
        Player targetPlayer = plugin.getServer().getPlayer(target);
        if (targetPlayer == null) return;
        if(targetPlayer.canSee(player)){
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
            EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    /*@EventHandler//TODO HAS MODE add in channel register
    public void onPlayerJoin(PlayerJoinEvent event){
        this.player_database.put(event.getPlayer().getUniqueId(), new BukkitNetworkInstance(event.getPlayer()));
    }*/

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();

        BukkitNetworkInstance instance = (BukkitNetworkInstance) this.player_database.remove(player.getUniqueId());
        if(instance != null) instance.closeConnection();
    }

    @EventHandler
    public void playerDies(EntityPoseChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Pose pose = event.getPose();
            if (pose == Pose.SNEAKING || pose == Pose.DYING || pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SLEEPING) {
                playerEntersInvalidPose((Player) event.getEntity());
            }
        }
    }

    @EventHandler//TODO HAS MODE
    public void playerRegisterChannel(PlayerRegisterChannelEvent event) {
        if(event.getChannel().equals(BukkitWrapper.EmotePacket)) {
            BukkitNetworkInstance playerNetwork = (BukkitNetworkInstance) player_database.getOrDefault(event.getPlayer().getUniqueId(), null);
            if (playerNetwork == null) {
                player_database.put(event.getPlayer().getUniqueId(), new BukkitNetworkInstance(event.getPlayer()));
            }
        }
    }
}
