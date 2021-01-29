package com.kosmx.emoteBukkit;

import com.kosmx.emotecraftCommon.EmotecraftConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class EmoteListener implements Listener {
    @EventHandler
    public void onPlayerOpenEmoteDiscoveryChannel(PlayerRegisterChannelEvent event){
        if(Bukkit.getPluginManager().isPluginEnabled("emotecraft") && event.getChannel().equals(BukkitMain.DiscPacket)) { //TODO
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(EmotecraftConstants.networkingVersion);
            event.getPlayer().sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), BukkitMain.DiscPacket, buf.array());
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
