package io.github.kosmx.emotes.bukkit;

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
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitWrapper extends JavaPlugin {
    public final static String EMOTE_PACKET = CommonData.getIDAsString(CommonData.playEmoteID);

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onLoad() {
        try { // Trying to increase the packet limit since the paper server is crap and severely limited
            StreamCodecUtils.replaceFallback(StreamCodecUtils.getThis(ServerboundCustomPayloadPacket.STREAM_CODEC),
                    (id) -> DiscardedPayload.codec(id, CommonData.MAX_PACKET_SIZE)
            );
        } catch (ReflectiveOperationException e) {
            CommonData.LOGGER.error("Failed to hack size! Try update your paper!", e);
            getServer().shutdown();
        }

        Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class); //it does register itself
        UniversalEmoteSerializer.loadEmotes();

        for (String permission : ServerCommands.PERMISSIONS) {
            Bukkit.getPluginManager().addPermission(new Permission(permission));
        }

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                ServerCommands.register(event.registrar().getDispatcher(), true)
        );
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, BukkitWrapper.EMOTE_PACKET);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, BukkitWrapper.EMOTE_PACKET, ServerSideEmotePlay.getInstance()::receivePluginMessage);
        getServer().getPluginManager().registerEvents(ServerSideEmotePlay.getInstance(), this);
        getLogger().info("Loading Emotecraft as a bukkit plugin...");
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, EMOTE_PACKET);
    }
}
