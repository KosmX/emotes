package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.bukkit.fuckery.StreamCodecUtils;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class BukkitWrapper extends JavaPlugin {

    public final static String EmotePacket = CommonData.getIDAsString(CommonData.playEmoteID);
    ServerSideEmotePlay networkPlay = null;

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onLoad() {
        if(CommonData.isLoaded){
            getLogger().warning("Emotecraft is loaded multiple times, please load it only once!");
            Bukkit.getPluginManager().disablePlugin(this); //disable itself.
        }
        else {
            CommonData.isLoaded = true;
        }

        try { // Trying to increase the packet limit since the paper server is crap and severely limited
            StreamCodecUtils.replaceFallback(StreamCodecUtils.getThis(ServerboundCustomPayloadPacket.STREAM_CODEC),
                    (id) -> DiscardedPayload.codec(id, CommonData.MAX_PACKET_SIZE)
            );
        } catch (ReflectiveOperationException e) {
            LoggerService.INSTANCE.log(Level.SEVERE, "Failed to hack size! Try update your paper!", e);
            getServer().shutdown();
        }

        Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class); //it does register itself
        UniversalEmoteSerializer.loadEmotes();

        for (String permission : ServerCommands.PERMISSIONS) {
            Bukkit.getPluginManager().addPermission(new Permission(permission, PermissionDefault.FALSE));
        }

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                ServerCommands.register(event.registrar().getDispatcher(), true)
        );
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
