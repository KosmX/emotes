package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
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

import java.util.HashMap;
import java.util.UUID;

public final class ServerSideEmotePlay extends AbstractServerEmotePlay<BukkitNetworkInstance> implements PluginMessageListener, Listener {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    private final HashMap<UUID, BukkitNetworkInstance> players = new HashMap<>();

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (channel.equals(BukkitWrapper.EMOTE_PACKET)) {
            BukkitNetworkInstance playerNetwork = this.players.get(player.getUniqueId());
            if (playerNetwork != null) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
                try {
                    this.receiveMessage(byteBuf, playerNetwork);
                } catch (Exception e) {
                    CommonData.LOGGER.error("", e);
                } finally {
                    byteBuf.release();
                }
            } else {
                CommonData.LOGGER.warn("Player {} is not registered!", player.getName());
            }
        }
    }

    @Override
    public UUID getUUIDFromPlayer(BukkitNetworkInstance player) {
        return player.avatar.getUUID();
    }

    @Override
    public BukkitNetworkInstance getPlayerFromUUID(UUID playerUuid) {
        if (!this.players.containsKey(playerUuid)) {
            CraftEntity entity = (CraftEntity) PLUGIN.getServer().getEntity(playerUuid);
            if (entity == null) return null;

            if (!(entity instanceof CraftMannequin)) {
                CommonData.LOGGER.error("Player {} never joined. If it is a fake player, the fake-player plugin forgot to fire join event.", entity);
            }
            this.players.put(playerUuid, new BukkitNetworkInstance((Avatar) entity.getHandle()));
        }
        return this.players.get(playerUuid);
    }

    @Override
    protected void sendForEveryoneElse(NetData data, BukkitNetworkInstance player) {
        for (Player player1 : player.avatar.getBukkitEntity().getTrackedBy()) {
            BukkitNetworkInstance instance = getPlayerFromUUID(player1.getUniqueId());
            if (instance == player) continue;

            // Bukkit server will filter if I really can send, or not.
            // If else to not spam dumb forge clients.
            if (player1.getListeningPluginChannels().contains(BukkitWrapper.EMOTE_PACKET)) {
                sendForPlayer(data, player, instance);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        registerPlayer(event.getPlayer());
    }

    @ApiStatus.Internal
    public void registerPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.players.containsKey(uuid)) return;
        this.players.put(uuid, new BukkitNetworkInstance(((CraftPlayer) player).getHandle()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        BukkitNetworkInstance instance = this.players.remove(event.getPlayer().getUniqueId());
        if (instance != null) instance.closeConnection();
    }

    @EventHandler
    public void onPlayerTrackEntity(PlayerTrackEntityEvent event) {
        if (((CraftEntity) event.getEntity()).getHandle() instanceof Avatar avatar) {
            playerStartTracking(getPlayerFromUUID(avatar.getUUID()), getPlayerFromUUID(event.getPlayer().getUniqueId()));
        }
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
