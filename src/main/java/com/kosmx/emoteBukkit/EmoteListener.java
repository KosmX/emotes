package com.kosmx.emoteBukkit;

import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class EmoteListener implements Listener {
    @EventHandler
    public void onPlayerOpenEmoteDiscoveryChannel(PlayerRegisterChannelEvent event){
        BukkitMain plugin = BukkitMain.getPlugin(BukkitMain.class);
        if(plugin.isEnabled() && event.getChannel().equals(BukkitMain.DiscPacket)) {
            if(BukkitMain.debug)plugin.getLogger().info("Sending Emotecraft version to player " + event.getPlayer().getName());
            DiscoveryPacket packet = new DiscoveryPacket(CommonData.networkingVersion);
            ByteBuf buf = Unpooled.buffer();
            packet.write(buf);
            event.getPlayer().sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), BukkitMain.DiscPacket, buf.array());
        }
        if(plugin.isEnabled() && event.getChannel().equals(BukkitMain.Emotepacket)
                && BukkitMain.player_database.get(event.getPlayer().getUniqueId()) == 0){
            BukkitMain.player_database.replace(event.getPlayer().getUniqueId(), 2);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        BukkitMain.player_database.put(event.getPlayer().getUniqueId(), 0);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        BukkitMain.player_database.remove(event.getPlayer().getUniqueId());
    }
}
