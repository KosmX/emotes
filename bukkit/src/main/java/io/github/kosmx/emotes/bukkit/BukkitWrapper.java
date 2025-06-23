package io.github.kosmx.emotes.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import io.github.kosmx.emotes.bukkit.executor.BukkitInstance;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class BukkitWrapper extends JavaPlugin {

    public final static String EmotePacket = CommonData.getIDAsString(CommonData.playEmoteID);
    public final static String GeyserPacket = "geyser:emote";
    ServerSideEmotePlay networkPlay = null;

    private ProtocolManager protocolManager;


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
        protocolManager = ProtocolLibrary.getProtocolManager();
        registerProtocolListener();
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

    private static final MinecraftVersion VERSION_1_20_2 = new MinecraftVersion(1, 20, 2);

    public void registerProtocolListener() {
        PacketType packetType = VERSION_1_20_2.atOrAbove() ? PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.NAMED_ENTITY_SPAWN;

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, packetType) {
            @Override
            public void onPacketSending(PacketEvent packetEvent) {
                if (packetEvent.getPacketType().equals(packetType)) {
                    //Field trackedField = packetEvent.getPacket().getStructures().getField(2);
                    UUID tracked = packetEvent.getPacket().getUUIDs().readSafely(0);

                    AbstractServerEmotePlay.getInstance().playerStartTracking(BukkitWrapper.this.networkPlay.getPlayerFromUUID(tracked), packetEvent.getPlayer());

                }
            }

            @Override
            public void onPacketReceiving(PacketEvent packetEvent) {

            }
        });
    }
}
