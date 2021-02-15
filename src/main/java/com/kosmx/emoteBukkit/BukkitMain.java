package com.kosmx.emoteBukkit;

import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraftCommon.Logger;
import com.kosmx.emotecraftCommon.Proxy;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import com.kosmx.emotecraftCommon.network.EmotePacket;
import com.kosmx.emotecraftCommon.network.StopPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
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
    @Nullable
    public FileConfiguration config = null;
    final EmoteListener listener = new EmoteListener();

    public static boolean validate = false;
    public static boolean debug = true;


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
            this.saveDefaultConfig();
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
        this.config = this.getConfig();
        validate = config.getBoolean("validation");
        debug = config.getBoolean("debug");
        getServer().getPluginManager().registerEvents(listener, this);
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Emotepacket, (channel, player, message) -> {
            if(debug) getLogger().info("[EMOTECRAFT] streaming emote");
            EmotePacket packet = new EmotePacket();
            if(!packet.read(Unpooled.wrappedBuffer(message), (float) this.config.getDouble("validThreshold")) && validate){
                getLogger().info("Player: " + player.getName() + " is playing an invalid emote");
                ByteBuf buf = Unpooled.buffer();
                StopPacket stopPacket = new StopPacket(player.getUniqueId());
                stopPacket.write(buf);
                player.sendPluginMessage(this, Stoppacket, buf.array());
                return;
            }

            packet.setPlayer(player.getUniqueId());
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer != player && otherPlayer.canSee(player)){
                    ByteBuf buf = Unpooled.buffer();
                    packet.write(buf, player_database.get(otherPlayer.getUniqueId()));
                    otherPlayer.sendPluginMessage(this, Emotepacket, buf.array());
                }
            }
        });
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Stoppacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Stoppacket, (channel, player, message) -> {
            if(debug)getLogger().info("[EMOTECRAFT] streaming emote stop");
            StopPacket packet = new StopPacket(player.getUniqueId());
            //packet.read(Unpooled.wrappedBuffer(message)); //Don't make exploitable code
            for(Player otherPlayer:getServer().getOnlinePlayers()){
                if(otherPlayer != player && otherPlayer.canSee(player)){
                    ByteBuf buf = Unpooled.buffer();
                    packet.write(buf);
                    otherPlayer.sendPluginMessage(this, Stoppacket, buf.array());
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
            if(debug)getLogger().info("Player " + player.getName() + " has Emotecraft networking-v" + ver + " installed.");
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Stoppacket);
    }
}
