package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import io.github.kosmx.emotes.server.network.instance.ServerNetworkInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;

import java.util.UUID;

public class BukkitNetworkInstance extends ServerNetworkInstance {
    protected final Avatar avatar;

    public BukkitNetworkInstance(ConfigNetworkInstance configInstance, Avatar avatar) {
        super(configInstance);
        this.avatar = avatar;
    }

    @Override
    public UUID getUUID() {
        return this.avatar.getUUID();
    }

    @Override
    public void sendMessage(EmotePacket.Builder packet, boolean updateVersions) {
        if (!isActive()) return; // isActive() guarantees the avatar is a ServerPlayer
        if (updateVersions) packet.setVersion(getVersions());
        ((ServerPlayer) this.avatar).connection.send(convertEmotePacket(packet.build()));
    }

    public static Packet<?> convertEmotePacket(EmotePacket packet) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            packet.write(buf, PacketBound.CLIENT);
            return new ClientboundCustomPayloadPacket(new DiscardedPayload(McUtils.EMOTE_CHANNEL_ID, MathHelper.readBytes(buf)));
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
