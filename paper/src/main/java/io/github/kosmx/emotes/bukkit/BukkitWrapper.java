package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.bukkit.fuckery.EmotePayloadHandler;
import io.github.kosmx.emotes.bukkit.fuckery.StreamCodecUtils;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.netty.channel.Channel;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BukkitWrapper extends JavaPlugin implements ChannelInitializeListener {
    public final static String EMOTE_PACKET = CommonData.getIDAsString(CommonData.playEmoteID);

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onLoad() {
        try { // Trying to increase the packet limit since the paper server is shit and severely limited
            StreamCodecUtils.replaceFallback(StreamCodecUtils.getThis(ServerboundCustomPayloadPacket.STREAM_CODEC),
                    (id) -> DiscardedPayload.codec(id, CommonData.MAX_PACKET_SIZE)
            );
        } catch (ReflectiveOperationException e) {
            CommonData.LOGGER.error("Failed to hack size! Try update your paper!", e);
            getServer().shutdown();
        }
        // Step two
        ChannelInitializeListenerHolder.addListener(Key.key(CommonData.MOD_ID, "listener"), this);

        Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class); //it does register itself
        UniversalEmoteSerializer.loadEmotes();

        for (String permission : ServerCommands.PERMISSIONS) {
            Bukkit.getPluginManager().addPermission(new Permission(permission));
        }

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                ServerCommands.register(event.registrar().getDispatcher(), true)
        );

        CommonData.LOGGER.info("Loading Emotecraft as a paper plugin...");
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, BukkitWrapper.EMOTE_PACKET);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, BukkitWrapper.EMOTE_PACKET, ServerSideEmotePlay.getInstance());
        getServer().getPluginManager().registerEvents(ServerSideEmotePlay.getInstance(), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, EMOTE_PACKET);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, EMOTE_PACKET);
        HandlerList.unregisterAll(ServerSideEmotePlay.getInstance());

        CommonData.LOGGER.warn("Emotecraft does not support disabling by PlugMan and similar plugins");
    }

    @Override
    public void afterInitChannel(@NotNull Channel channel) {
        channel.pipeline().addAfter("splitter", BukkitWrapper.EMOTE_PACKET, EmotePayloadHandler.INSTANCE);
    }
}
