package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.server.network.instance.ServerNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;

import java.util.UUID;

public class BukkitNetworkInstance extends ServerNetworkInstance {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    protected final Avatar avatar;

    public BukkitNetworkInstance(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public UUID getUUID() {
        return this.avatar.getUUID();
    }

    @Override
    public void sendMessage(EmotePacket.Builder packet, boolean updateVersions) {
        if (!(this.avatar instanceof ServerPlayer player)) {
            CommonData.LOGGER.error("Attempt to send a packet of an unsupported entity: {}!", this.avatar);
            return;
        }
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            if (updateVersions) packet.setVersion(getVersions());
            packet.build().write(buf, PacketBound.CLIENT);
            player.getBukkitEntity().sendPluginMessage(PLUGIN, BukkitWrapper.EMOTE_PACKET, MathHelper.readBytes(buf));
        } finally {
            buf.release();
        }
    }

    @Override
    public void disconnect() {
        // no-op (client-only)
    }

    @Override
    public boolean isActive() {
        return this.avatar instanceof ServerPlayer;
    }
}
