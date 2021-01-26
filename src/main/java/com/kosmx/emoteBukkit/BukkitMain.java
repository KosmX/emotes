package com.kosmx.emoteBukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMain extends JavaPlugin {

    final static String Emotepacket = "emotecraft:playemote";
    final static String Stoppacket = "emotecraft:stopemote";

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Emotepacket, (channel, player, message) -> {
            getLogger().info("[EMOTECRAFT] streaming emote");
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer.canSee(player)){
                    otherPlayer.sendPluginMessage(this, Emotepacket, message);
                }
            }
        });
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Stoppacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Stoppacket, (channel, player, message) -> {
            getLogger().info("[EMOTECRAFT] streaming emote");
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer.canSee(player)){
                    otherPlayer.sendPluginMessage(this, Stoppacket, message);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Stoppacket);
    }
}
