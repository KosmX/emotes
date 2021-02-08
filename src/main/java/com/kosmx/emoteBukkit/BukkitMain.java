package com.kosmx.emoteBukkit;

import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraftCommon.Logger;
import com.kosmx.emotecraftCommon.Proxy;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import com.kosmx.emotecraftCommon.network.EmotePacket;
import com.kosmx.emotecraftCommon.network.StopPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
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

    public static boolean validate = false;


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
            EmotePacket packet = new EmotePacket();
            if(!packet.read(Unpooled.wrappedBuffer(message)) && validate){
                getLogger().info("Player: " + player.getName() + " is playing an invalid emote");
                return;
            }
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
            getLogger().info("[EMOTECRAFT] streaming emote");
            StopPacket packet = new StopPacket();
            packet.read(Unpooled.wrappedBuffer(message));
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
