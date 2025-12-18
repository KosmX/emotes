package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BukkitNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    private static final BukkitWrapper PLUGIN = BukkitWrapper.getPlugin(BukkitWrapper.class);

    private final EmotePlayTracker emotePlayTracker = new EmotePlayTracker();
    protected final Avatar avatar;

    public BukkitNetworkInstance(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emotePlayTracker;
    }

    @Override
    public void sendMessage(EmotePacket packet, @Nullable UUID target) {
        if (!(this.avatar instanceof ServerPlayer player)) {
            CommonData.LOGGER.error("Attempt to send a packet of an unsupported entity: {}!", this.avatar);
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        try {
            packet.write(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            player.getBukkitEntity().sendPluginMessage(PLUGIN, BukkitWrapper.EMOTE_PACKET, bytes);
        } finally {
            buf.release();
        }
    }

    @Override
    public boolean isActive() {
        return this.avatar instanceof ServerPlayer;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void presenceResponse() {
        super.presenceResponse();
        ServerSideEmotePlay.getInstance().presenceResponse(this, trackPlayState());
    }
}
