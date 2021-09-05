package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.bukkit.executor.BukkitInstance;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitWrapper extends JavaPlugin {

    public final static String EmotePacket = CommonData.getIDAsString(CommonData.playEmoteID);
    public final static String GeyserPacket = "geyser:emote";
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
        Serializer.INSTANCE = new Serializer(); //it does register itself
        EmoteInstance.config = Serializer.getConfig();
        UniversalEmoteSerializer.loadEmotes();
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
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, EmotePacket);
    }
}
