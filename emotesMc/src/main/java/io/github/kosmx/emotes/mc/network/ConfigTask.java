package io.github.kosmx.emotes.mc.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ConfigTask implements ConfigurationTask {
    public static final Type TYPE = new Type("emotes:config");

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) {
        try {
            EmotePacket packet = INetworkInstance.createConfigPacket(true).build();
            consumer.accept(convert(packet)); // Config init
        } catch (Throwable e) {
            CommonData.LOGGER.warn("Failed to configure client!", e);
        }
    }

    protected abstract Packet<?> convert(EmotePacket packet);

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
