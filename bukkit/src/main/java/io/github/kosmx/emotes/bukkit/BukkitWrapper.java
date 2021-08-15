package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.bukkit.executor.BukkitInstance;
import io.github.kosmx.emotes.bukkit.network.BukkitNetworkInstance;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitWrapper extends JavaPlugin {

    public final static String Emotepacket = CommonData.getIDAsString(CommonData.playEmoteID);
    ServerSideEmotePlay networkPlay = null;


    @Override
    public void onLoad() {
        if(CommonData.isLoaded){
            getLogger().warning("Emotecraft is loaded multiple times, please load it only once!");
            Bukkit.getPluginManager().disablePlugin(this); //disable itself.
        }
        else {
            CommonData.isLoaded = true;
        }
        EmoteInstance.instance = new BukkitInstance(this);
        new Serializer(); //it does register itself
        EmoteInstance.config = Serializer.getConfig();
        UniversalEmoteSerializer.serializeServerEmotes();
    }

    @Override
    public void onEnable() {
        this.networkPlay = new ServerSideEmotePlay(this);
        getServer().getPluginManager().registerEvents(networkPlay,this);
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Emotepacket);
    }
}
