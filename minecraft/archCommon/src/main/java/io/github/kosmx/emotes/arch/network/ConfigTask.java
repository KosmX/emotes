package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class ConfigTask implements ConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new Type("emotes:config");

    @Override
    public void start(@NotNull Consumer<Packet<?>> consumer) {
        NetData configData = new EmotePacket.Builder().configureToConfigExchange(true).build().data;
        configData.versions.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)0x01); // track player state
        try {
            var bytes = new EmotePacket.Builder(configData).build().write();
            consumer.accept(NetworkPlatformTools.playPacket(bytes)); // Config init
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to configure client!", e);
        }
    }

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
