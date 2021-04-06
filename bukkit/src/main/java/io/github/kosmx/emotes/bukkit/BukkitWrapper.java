package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class BukkitWrapper extends JavaPlugin {

    final static String Emotepacket = CommonData.getIDAsString(CommonData.playEmoteID);
    @Nullable
    public FileConfiguration config = null;
    final EventListener listener = new EventListener();

    public static boolean validate = false;
    public static boolean debug = true;


    static HashMap<UUID, Integer> player_database = new HashMap<>();

    @Override
    public void onLoad() {
        if(CommonData.isLoaded){
            getLogger().warning("Emotecraft is loaded multiple times, please load it only once!");
            Bukkit.getPluginManager().disablePlugin(this); //disable itself.
        }
        else {
            CommonData.isLoaded = true;
        }
    }

    /*
     * Weird way to check Fabric loader
     * @return is Emotecraft installed as a Fabric mod
     * (I hope, it will be idiot-proof :D
     *
    private boolean checkForFabricInstance(){
        try{
             return (boolean) Class.forName("net.fabricmc.loader.api.FabricLoader").getMethod("isModLoaded", String.class).invoke(Class.forName("net.fabricmc.loader.api.FabricLoader").getMethod("getInstance").invoke(null), "emotecraft");
             //FabricLoader.genInstance().isModLoaded("emotecraft"); //without classpath in compile/runtime
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException exception){
            //If it didn't work, no problem
            return false;
        }
    }//*/

    @Override
    public void onEnable() {
        this.config = this.getConfig();
        validate = config.getBoolean("validation");
        debug = config.getBoolean("debug");
        getServer().getPluginManager().registerEvents(listener, this);
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Emotepacket);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, Emotepacket, this::receivePluginMessage);
    }

    private void receivePluginMessage(String ignore, Player player, byte[] message){

        if(debug) getLogger().info("[EMOTECRAFT] streaming emote");
        try {
            NetData data = new EmotePacket.Builder().configureValidationThreshold((float) this.config.getDouble("validThreshold")).build().read(ByteBuffer.wrap(message));
            if(data == null || data.purpose == null)throw new IOException("No data received");
            if(!data.purpose.isEmoteStream){
                if(data.purpose == PacketTask.CONFIG){
                    player_database.replace(player.getUniqueId(), 8);
                    if(debug)getLogger().info("Player " + player.getName() + " has Emotecraft installed.");
                }
            }
            else {
                if(data.purpose == PacketTask.STREAM && !data.valid && config.getBoolean("validation")){
                    player.sendPluginMessage(this, Emotepacket, new EmotePacket.Builder().configureToSendStop(data.emoteData.hashCode(), player.getUniqueId()).build().write().array());
                }
                else {
                    Player target = null;
                    if (data.player != null) {
                        target = getServer().getPlayer(data.player);
                    }
                    data.player = player.getUniqueId();
                    byte[] bytes = new EmotePacket.Builder(data).build().write().array();
                    if (target != null) {
                        if (target != player && target.canSee(player)) {
                            target.sendPluginMessage(this, Emotepacket, bytes);
                        }
                    }
                    else {
                        for (Player target1 : getServer().getOnlinePlayers()) {
                            if (target1 != player && target1.canSee(player)) {
                                target1.sendPluginMessage(this, Emotepacket, bytes);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, Emotepacket);
    }
}
