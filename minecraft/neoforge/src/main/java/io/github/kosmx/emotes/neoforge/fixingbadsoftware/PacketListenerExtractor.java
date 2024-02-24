package io.github.kosmx.emotes.neoforge.fixingbadsoftware;

import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class PacketListenerExtractor {

    /**
     * It might work, it might now, I just don't want to with any forge related platform anymore.
     * Their new API looks cool, but it is even worse than having nothing
     * @param context It has the info I need, I just need to extract it
     * @return something that should be accessible using context.connection()
     */
    public static @NotNull ServerConfigurationPacketListenerImpl extract(ConfigurationPayloadContext context) {
        try {
            var f = context.packetHandler().getClass().getDeclaredField("listener");
            f.setAccessible(true);
            return (ServerConfigurationPacketListenerImpl) f.get(context.packetHandler());
        } catch (Exception e) {
            throw new RuntimeException("NeoForge is retard", e);
        }
    }
}
