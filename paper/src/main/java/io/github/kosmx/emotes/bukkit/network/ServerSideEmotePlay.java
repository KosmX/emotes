package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.bukkit.fuckery.StreamCodecUtils;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.connection.DisconnectionReason;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.Avatar;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftMannequin;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerSideEmotePlay extends AbstractServerEmotePlay<BukkitNetworkInstance> implements PluginMessageListener, Listener {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    private final Map<Connection, ConfigNetworkInstance> configs = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitNetworkInstance> players = new ConcurrentHashMap<>();

    public void onPongMessageReceived(ServerConfigurationPacketListenerImpl impl, int time) {
        if (time == PaperConfigTask.PING_MAGIC_INT && PaperConfigTask.ON_CONFIG.remove(impl.connection)) {
            CommonData.LOGGER.warn("Client doesn't support emotes, ignoring!"); // No disconnect, vanilla clients can connect
            impl.finishCurrentTask(PaperConfigTask.TYPE);
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageReceived(@NotNull String channel, @NotNull PlayerConnection connection, byte @NotNull [] message) {
        if (!BukkitWrapper.EMOTE_PACKET.equals(channel)) return;
        if (!(connection instanceof PlayerConfigurationConnection configuration)) return;

        ServerConfigurationPacketListenerImpl listener = (ServerConfigurationPacketListenerImpl) StreamCodecUtils.PACKET_LISTENER.get(configuration);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
        try {
            PaperConfigTask.ON_CONFIG.remove(listener.connection);
            this.configs.computeIfAbsent(listener.connection, _ -> new ConfigNetworkInstance())
                    .receiveConfigMessage(new EmotePacket(byteBuf, PacketBound.SERVER), emotePacket -> listener.send(BukkitNetworkInstance.convertEmotePacket(emotePacket)));
            listener.finishCurrentTask(PaperConfigTask.TYPE); // And, we're done here
        } catch (Exception e) {
            CommonData.LOGGER.error("Invalid Emotecraft packet!", e);
            listener.disconnect(Component.literal(CommonData.MOD_ID + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())), DisconnectionReason.INVALID_PAYLOAD);
        } finally {
            byteBuf.release();
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (channel.equals(BukkitWrapper.EMOTE_PACKET)) {
            BukkitNetworkInstance playerNetwork = this.players.get(player.getUniqueId());
            if (playerNetwork != null) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
                try {
                    this.receiveMessage(new EmotePacket(byteBuf, PacketBound.SERVER), playerNetwork);
                } catch (Exception e) {
                    CommonData.LOGGER.error("Invalid Emotecraft packet from {}!", player.getName(), e);
                } finally {
                    byteBuf.release();
                }
            } else {
                CommonData.LOGGER.warn("Player {} is not registered!", player.getName());
            }
        }
    }

    @Override
    public BukkitNetworkInstance getPlayerFromUUID(UUID playerUuid) {
        if (!this.players.containsKey(playerUuid)) {
            CraftEntity entity = (CraftEntity) PLUGIN.getServer().getEntity(playerUuid);
            if (entity == null) return null;

            if (!(entity instanceof CraftMannequin)) {
                CommonData.LOGGER.error("Player {} never joined. If it is a fake player, the fake-player plugin forgot to fire join event.", entity);
            }
            ConfigNetworkInstance instance = ConfigNetworkInstance.IMMUTABLE;
            if (entity instanceof CraftPlayer player) {
                ConfigNetworkInstance configured = this.configs.get(player.getHandle().connection.connection);
                instance = configured == null ? new ConfigNetworkInstance() : configured;
            }
            this.players.put(playerUuid, new BukkitNetworkInstance(instance, (Avatar) entity.getHandle()));
        }
        return this.players.get(playerUuid);
    }

    @Override
    protected void sendForTrackedBy(NetData data, BukkitNetworkInstance player) {
        for (Player player1 : player.avatar.getBukkitEntity().getTrackedBy()) {
            BukkitNetworkInstance instance = getPlayerFromUUID(player1.getUniqueId());
            if (instance == null || instance == player) continue;

            // Bukkit server will filter if I really can send, or not.
            // If else to not spam dumb forge clients.
            if (player1.getListeningPluginChannels().contains(BukkitWrapper.EMOTE_PACKET)) {
                instance.sendMessage(data, true);
            }
        }
    }

    @Override
    protected void sendForEveryone(NetData data) {
        for (BukkitNetworkInstance instance : this.players.values()) instance.sendMessage(data, true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        registerPlayer(((CraftPlayer)event.getPlayer()).getHandle());
    }

    @ApiStatus.Internal
    public void registerPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (this.players.containsKey(uuid)) return;
        ConfigNetworkInstance configured = this.configs.get(player.connection.connection);
        this.players.put(uuid, new BukkitNetworkInstance(configured == null ? new ConfigNetworkInstance() : configured, player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BukkitNetworkInstance instance = this.players.remove(event.getPlayer().getUniqueId());
        if (instance != null) instance.disconnect();
        this.configs.remove(((CraftPlayer) event.getPlayer()).getHandle().connection.connection);
    }

    @EventHandler
    public void onPlayerTrackEntity(PlayerTrackEntityEvent event) {
        if (((CraftEntity) event.getEntity()).getHandle() instanceof Avatar avatar) {
            playerStartTracking(getPlayerFromUUID(avatar.getUUID()), getPlayerFromUUID(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    public void onPlayerConnectionInitialConfigure(PlayerConnectionInitialConfigureEvent event) {
        ServerCommonPacketListenerImpl impl = (ServerCommonPacketListenerImpl) StreamCodecUtils.PACKET_LISTENER.get(event.getConnection());
        ((Queue<ConfigurationTask>) StreamCodecUtils.CONFIGURATION_TASKS.get(impl)).add(new PaperConfigTask(impl));
    }

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    public static ServerSideEmotePlay getInstance() {
        return (ServerSideEmotePlay) AbstractServerEmotePlay.getInstance();
    }
}
