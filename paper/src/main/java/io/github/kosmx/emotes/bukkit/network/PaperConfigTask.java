package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.mc.network.ConfigTask;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PaperConfigTask extends ConfigTask {
    protected static final Set<Connection> ON_CONFIG = ConcurrentHashMap.newKeySet();
    public static final int PING_MAGIC_INT = 0xEC7AF7;

    private final ServerCommonPacketListenerImpl impl;

    public PaperConfigTask(ServerCommonPacketListenerImpl impl) {
        this.impl = impl;
    }

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) {
        PaperConfigTask.ON_CONFIG.add(this.impl.connection);
        super.start(consumer);
        consumer.accept(new ClientboundPingPacket(PING_MAGIC_INT));
    }

    @Override
    protected Packet<?> convert(EmotePacket packet) {
        return BukkitNetworkInstance.convertEmotePacket(packet);
    }
}
