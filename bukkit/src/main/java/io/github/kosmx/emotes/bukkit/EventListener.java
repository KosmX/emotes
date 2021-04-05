package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.common.network.EmotePacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import java.io.IOException;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerOpenEmoteDiscoveryChannel(PlayerRegisterChannelEvent event){
        BukkitWrapper plugin = BukkitWrapper.getPlugin(BukkitWrapper.class);
        if(plugin.isEnabled() && event.getChannel().equals(BukkitWrapper.Emotepacket)) {
            /*
            if(BukkitWrapper.debug)plugin.getLogger().info("Sending Emotecraft version to player " + event.getPlayer().getName());
            DiscoveryPacket packet = new DiscoveryPacket(CommonData.networkingVersion);
            ByteBuf buf = Unpooled.buffer();
            packet.write(buf);
            event.getPlayer().sendPluginMessage(BukkitWrapper.getPlugin(BukkitWrapper.class), BukkitWrapper.DiscPacket, buf.array());
             */
            try {
                event.getPlayer().sendPluginMessage(BukkitWrapper.getPlugin(BukkitWrapper.class), BukkitWrapper.Emotepacket, new EmotePacket.Builder().configureToConfigExchange(false).build().write().array());
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        if(plugin.isEnabled() && event.getChannel().equals(BukkitWrapper.Emotepacket)
                && BukkitWrapper.player_database.get(event.getPlayer().getUniqueId()) == 0){
            BukkitWrapper.player_database.replace(event.getPlayer().getUniqueId(), 2);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        BukkitWrapper.player_database.put(event.getPlayer().getUniqueId(), 0);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        BukkitWrapper.player_database.remove(event.getPlayer().getUniqueId());
    }

}
