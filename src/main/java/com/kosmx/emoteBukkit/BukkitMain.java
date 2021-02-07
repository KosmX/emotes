package com.kosmx.emoteBukkit;

import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraftCommon.Logger;
import com.kosmx.emotecraftCommon.Proxy;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

/**
 * Main class for Bukkit plugin.
 * If, this will be invoke, no Minecraft classes will be available. Don't use these from here.
 */
public class BukkitMain extends JavaPlugin {

    final static String Emotepacket = CommonData.getIDAsString(CommonData.playEmoteID);
    final static String Stoppacket = CommonData.getIDAsString(CommonData.stopEmoteID);
    final static String DiscPacket = CommonData.getIDAsString(CommonData.discoverEmoteID);
    final EmoteListener listener = new EmoteListener();


    static HashMap<UUID, Integer> player_database = new HashMap<>();

    @Override
    public void onLoad() {
        if(CommonData.isLoaded || checkForFabricInstance()){
            getLogger().warning("Emotecraft is ALREADY loaded as a Fabric mod. Don't load it twice!");
            Bukkit.getPluginManager().disablePlugin(this); //disable itself.
        }
        else {
            CommonData.isLoaded = true;
            CommonData.logger = new Logger() {
                @Override
                public void log(String msg) {
                    this.log(msg);
                }

                @Override
                public void warn(String msg) {
                    this.warn(msg);
                }

                @Override
                public void error(String msg) {
                    this.error(msg);
                }
            };
        }
    }

    /**
     * Weird way to check Fabric loader
     * @return is Emotecraft installed as a Fabric mod
     * (I hope, it will be idiot-proof :D
     */
    private boolean checkForFabricInstance(){
        try{
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return Proxy.isLoadedAsFabricMod();
        }
        catch (ClassNotFoundException exception){
            return false;
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(listener, this);
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Emotepacket, (channel, player, message) -> {
            getLogger().info("[EMOTECRAFT] streaming emote");
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer != player && otherPlayer.canSee(player)){
                    otherPlayer.sendPluginMessage(this, Emotepacket, message);
                }
            }
        });
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Stoppacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Stoppacket, (channel, player, message) -> {
            getLogger().info("[EMOTECRAFT] streaming emote");
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer != player && otherPlayer.canSee(player)){
                    otherPlayer.sendPluginMessage(this, Stoppacket, message);
                }
            }
        });
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, DiscPacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, DiscPacket, (channel, player, message) -> {
            DiscoveryPacket packet = new DiscoveryPacket();
            ByteBuf reader = Unpooled.copiedBuffer(message);
            packet.read(reader);
            int ver = packet.getVersion();
            player_database.replace(player.getUniqueId(), ver);
            getLogger().info("Player " + player.getName() + " has Emotecraft v" + ver + " installed.");
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Stoppacket);
    }
}
